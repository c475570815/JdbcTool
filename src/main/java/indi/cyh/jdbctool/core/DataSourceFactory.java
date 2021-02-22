package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.config.DataSourceConfig;
import indi.cyh.jdbctool.config.DbConfig;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.DbTemplate;
import indi.cyh.jdbctool.tool.JdbcUrlTool;
import indi.cyh.jdbctool.tool.StringTool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * DataSourceFactory
 *
 * @ClassName DataSourceFactory
 * @Description TODO
 * @Author gm
 * @Date 2020/7/16 20:40
 */
public class DataSourceFactory {


    //静态控制锁
    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();

    private static final ReentrantReadWriteLock.ReadLock rl;
    private static final ReentrantReadWriteLock.WriteLock wl;
    /**
     * 默认主配数据库
     */
    private static JdbcDataBase mianDb;

    /**
     * 静态连接池
     */
    private static final ConcurrentHashMap<DbInfo, JdbcDataBase> listDbSource = new ConcurrentHashMap<>();


    static {
        //锁赋值
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
        DbConfig.loadConfig();
    }


    /**
     * 生成一个数据源
     *
     * @param entity
     * @param config
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author cyh
     * 2020/7/16 21:10
     **/
    private static JdbcDataBase getJdbcBaseByInfo(DbInfo entity, DbTemplate config) throws Exception {
        boolean hasTemplate = config != null;
        if (hasTemplate) {
            //使用给出的模板加载db连接实体类
            setDbInfo(entity, config);
        } else if (StringTool.isNotEmpty(entity.getType()) && StringTool.isEmpty(entity.getConnectStr())) {
            //把配置结合参数转换为db连接实体类
            setDbInfoByDefaultUrlTemplate(entity);
        }
        //根据实体生成数据源
        JdbcDataBase db = getExitJdbcBase(entity);
        if (db == null) {
            db = getNewJdbcBase(entity);
        }
        return db;
    }

    /**
     * 获取db操作实体
     *
     * @param entity 数据库信息
     * @return indi.cyh.jdbctool.main.JdbcDataBase
     * @author cyh
     * 2020/7/16 21:46
     **/
    public static JdbcDataBase getDb(DbInfo entity) throws Exception {
        return getJdbcBaseByInfo(entity, null);
    }

    /**
     * 获取主配
     *
     * @param
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author cyh
     * 2020/7/16 21:13
     **/
    public static JdbcDataBase getMianDb() {
        return mianDb;
    }

    /**
     * 获取已存在的连接池
     *
     * @param dbInfo
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author CYH
     * @date 2020/7/10 0010 16:00
     **/
    private static JdbcDataBase getExitJdbcBase(DbInfo dbInfo) {
        rl.lock();
        try {
            for (DbInfo info : listDbSource.keySet()) {
                if (DbInfo.equals(info, dbInfo)) {
                    System.out.println("获取到已有连接池:" + info.getConnectStr());
                    System.out.println("目前连接池数量: " + listDbSource.size());
                    return listDbSource.get(info);
                }
            }
        } catch (Exception e) {
            System.out.println("获取已有连接池时异常: " + e.getMessage());
        } finally {
            rl.unlock();
        }
        return null;
    }

    /**
     * 加载给出的数据源信息
     *
     * @param entity 配置文件读取的默认配置
     * @return void
     * @author cyh
     * 2020/5/28 21:57
     **/
    private static void setDbInfoByDefaultUrlTemplate(DbInfo entity) throws Exception {
        for (DbTemplate config : DbConfig.getDefaultUrlTemplateMap().values()) {
            if (entity.getType().equals(config.getType())) {
                setDbInfo(entity, config);
                return;
            }
        }
        throw new Exception("不支持的数据源类型!");
    }

    private static void setDbInfo(DbInfo entity, DbTemplate config) {
        entity.setConnectStr(getDbConnectUrl(config, entity));
        entity.setDriverClassName(config.getDriverClassName());
        if (entity.getPort() == null) {
            entity.setPort(config.getPort());
        }
    }

    /**
     * 根据db信息生成对应的jdbc链接
     *
     * @param config
     * @param entity
     * @return java.lang.String
     * @author cyh
     * 2020/5/28 21:59
     **/
    private static String getDbConnectUrl(DbTemplate config, DbInfo entity) {
        String urlTemplate;
        if (StringTool.isEmpty(entity.getConnectStr())) {
            urlTemplate = config.getJdbcTemplate();
            urlTemplate = urlTemplate.replace(DbConfig.IP, entity.getIp());
            urlTemplate = urlTemplate.replace(DbConfig.PORT, String.valueOf(entity.getPort()));
            urlTemplate = urlTemplate.replace(DbConfig.END_PARAM, entity.getEndParam());
        } else {
            urlTemplate = entity.getConnectStr();
        }
        return urlTemplate;
    }

    /**
     * 加载默认主配
     *
     * @return void
     * @author cyh
     * 2020/5/28 21:55
     **/
    public static void loadMainDbConfig() {
        try {
            List<DbInfo> defalutDatasource = DbConfig.getDefalutDatasource();
            if (defalutDatasource.size() > 0) {
                DbInfo mainDb = defalutDatasource.get(0);
                setDbInfoByDefaultUrlTemplate(mainDb);
                mianDb = getNewJdbcBase(mainDb);
            } else {
                throw new Exception("配置文件中没有配置数据库信息!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("主数据库加载失败! ---" + e.getMessage());
        }
    }

    /**
     * 生成新的连接池
     *
     * @param dbInfo
     * @return void
     * @author CYH
     * @date 2020/7/10 0010 15:49
     **/
    private static JdbcDataBase getNewJdbcBase(DbInfo dbInfo) {
        System.out.println("新增连接池-连接:  " + dbInfo.getConnectStr());
        DruidDataSource dataSource;
        //若DbInfo 中带有druid参数配置则不使用默认配置
        if (dbInfo.getDruidDataSource() != null) {
            dataSource = dbInfo.getDruidDataSource().cloneDruidDataSource();
        } else {
            dataSource = DataSourceConfig.getDefaultDataSource();
        }
        dataSource.setDriverClassName(dbInfo.getDriverClassName());
        dataSource.setUrl(dbInfo.getConnectStr());
        dataSource.setUsername(dbInfo.getLoginName());
        dataSource.setPassword(dbInfo.getPwd());
        try {
            Map<String, Object> dataInfoMap = JdbcUrlTool.findDataInfoMapByUrl(dbInfo.getConnectStr());
            dataSource.setName(dataInfoMap.get("type") + "-" + dataInfoMap.get("ip") + "-" + dbInfo.getLoginName());
            dataSource.setDbType(dataInfoMap.get("type").toString());
        } catch (Exception ignored) {

        }
        //监控设置
        try {
            dataSource.setFilters("stat,wall");
            dataSource.setEnable(true);
        } catch (Exception ignored) {

        }
        JdbcDataBase newDb = new JdbcDataBase(dataSource);
        wl.lock();
        try {
            listDbSource.put(dbInfo, newDb);
        } catch (Exception e) {
            System.out.println("生成新连接池时异常: " + e.getMessage());
            dataSource.close();
            listDbSource.remove(dbInfo);
        } finally {
            wl.unlock();
        }
        System.out.println("目前连接池数量: " + listDbSource.size());
        return newDb;
    }

    /**
     * 根据配置文件中的sourceName来获取JdbcDataBase
     *
     * @param SourceName
     * @return indi.cyh.jdbctool.core.JdbcDataBase
     * @author CYH
     * @date 2020/12/21 0021 15:34
     **/
    public static JdbcDataBase getDbBySourceName(String SourceName) {
        try {
            DbInfo entity = new DbInfo();
            List<DbInfo> defalutDatasource = DbConfig.getDefalutDatasource();
            if (defalutDatasource.size() > 0) {
                for (DbInfo dbInfo : defalutDatasource) {
                    if (SourceName.equals(dbInfo.getSourceName())) {
                        entity = dbInfo;
                        return getDb(entity);
                    }
                }
            }
            throw new Exception(SourceName + "加载失败!");
        } catch (Exception e) {
            System.out.println(SourceName + "加载失败!");
            e.printStackTrace();
        }
        return null;
    }
}
