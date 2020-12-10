package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.config.DbConfig;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.DbTemplate;
import indi.cyh.jdbctool.tool.JdbcUrlTool;
import indi.cyh.jdbctool.tool.StringTool;

import javax.sql.DataSource;
import java.util.*;
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
    private static DruidDataSource mianDataSource;

    /**
     * 静态连接池
     */
    private static final Map<DbInfo, DruidDataSource> listDbSource = new LinkedHashMap<>();


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
    private static DruidDataSource getDataSource(DbInfo entity, DbTemplate config) throws Exception {
        boolean hasTemplate = config != null;
        if (hasTemplate) {
            //使用给出的模板加载db连接实体类
            setDbInfo(entity, config);
        } else if (StringTool.isNotEmpty(entity.getType()) && StringTool.isEmpty(entity.getConnectStr())) {
            //把配置结合参数转换为db连接实体类
            setDbInfoByDefaultUrlTemplate(entity);
        }
        //根据实体生成数据源
        DruidDataSource dataSource = getExitDataSource(entity);
        if (dataSource == null) {
            dataSource = getNewDataSource(entity);
        }
        return dataSource;
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
        DruidDataSource source = getDataSource(entity, null);
        return new JdbcDataBase(source);
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
        return new JdbcDataBase(mianDataSource);
    }

    /**
     * 获取已存在的连接池
     *
     * @param dbInfo
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author CYH
     * @date 2020/7/10 0010 16:00
     **/
    private static DruidDataSource getExitDataSource(DbInfo dbInfo) {
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
        String urlTemplate = "";
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
            DbInfo entity = new DbInfo();
            List<DbInfo> defalutDatasource = DbConfig.getDefalutDatasource();
            if (defalutDatasource.size() > 0) {
                DbInfo mainDb = defalutDatasource.get(0);
                setDbInfoByDefaultUrlTemplate(mainDb);
                mianDataSource = getNewDataSource(mainDb);
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
    private static DruidDataSource getNewDataSource(DbInfo dbInfo) {
        System.out.println("新增连接池-连接:  " + dbInfo.getConnectStr());
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbInfo.getDriverClassName());
        dataSource.setUrl(dbInfo.getConnectStr());
        dataSource.setUsername(dbInfo.getLoginName());
        dataSource.setPassword(dbInfo.getPwd());
        try {
            Map<String, Object> dataInfoMap = JdbcUrlTool.findDataInfoMapByUrl(dbInfo.getConnectStr());
            dataSource.setName(dataInfoMap.get("type") + "-" + dataInfoMap.get("ip") + "-" + dbInfo.getLoginName());
            dataSource.setDbType(dataInfoMap.get("type").toString());
        } catch (Exception e) {

        }
        //监控设置
        try {
            //,2
            dataSource.setFilters("stat,wall,log4j");
            dataSource.setEnable(true);
        } catch (Exception e) {

        }
        //配置初始化大小、最小、最大
        dataSource.setInitialSize(10);
        dataSource.setMinIdle(10);
        //最大活跃数
        dataSource.setMaxActive(20);
        //配置从连接池获取连接等待超时的时间
        dataSource.setMaxWait(10000);
        //配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
        //检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
        // 2.在minIdle以内的不处理。
        dataSource.setTimeBetweenEvictionRunsMillis(600000);
        // 配置一个连接在池中最大空闲时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);
        //  验证连接语句
        // dataSource.setValidationQuery(false);
        //设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查
        dataSource.setTestWhileIdle(false);
        //设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        dataSource.setTestOnBorrow(false);
        // 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        dataSource.setTestOnReturn(false);
        //打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料
        dataSource.setPoolPreparedStatements(true);
        //
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        //根据自身业务及事务大小来设置
        dataSource.setConnectionProperties("oracle.net.CONNECT_TIMEOUT=6000;oracle.jdbc.ReadTimeout=180000");
        //打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性. 参考：https://github.com/alibaba/druid/wiki/KeepAlive_cn
        dataSource.setKeepAlive(true);
        // 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。
        dataSource.setRemoveAbandoned(true);

        dataSource.setRemoveAbandonedTimeout(80);
        wl.lock();
        try {
            listDbSource.put(dbInfo, dataSource);
        } catch (Exception e) {
            System.out.println("生成新连接池时异常: " + e.getMessage());
            dataSource.close();
            listDbSource.remove(dbInfo);
        } finally {
            wl.unlock();
        }
        System.out.println("目前连接池数量: " + listDbSource.size());
        return dataSource;
    }
}
