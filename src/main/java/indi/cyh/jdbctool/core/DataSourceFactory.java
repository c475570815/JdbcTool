package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.config.ConfigCenter;
import indi.cyh.jdbctool.config.TemplateConfig;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.tool.JdbcUrlTool;
import indi.cyh.jdbctool.tool.StringTool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataSourceFactory
 *
 * @ClassName DataSourceFactory
 * @Description TODO
 * @Author gm
 * @Date 2020/7/16 20:40
 */
public class DataSourceFactory {
    /**
     * 主库sourceName
     */
    private final static String MIAN_DB_SOURCENAME = "mainDb";


    /**
     * 静态连接池  DbInfo JdbcDataBase
     */
    private static final ConcurrentHashMap<DbInfo, JdbcDataBase> listDbSource = new ConcurrentHashMap<>();

    /**
     * 获取配置的主库(sourceName为mainDb的配置)
     *
     * @param
     * @return indi.cyh.jdbctool.core.JdbcDataBase
     * @author CYH
     * @date 2021/2/25 0025 9:29
     **/
    public static JdbcDataBase getJdbcDataBase() {
        return getJdbcDataBase(MIAN_DB_SOURCENAME);
    }

    /**
     * 通过配置的db连接信息中的sourceName属性获取db信息
     *
     * @param name
     * @return indi.cyh.jdbctool.core.JdbcDataBase
     * @author CYH
     * @date 2021/2/25 0025 9:31
     **/
    public static JdbcDataBase getJdbcDataBase(String name) {
        JdbcDataBase jdbcDataBase;
        DbInfo dbInfo = listDbSource.keySet().stream()
                .filter(info -> info.getSourceName().equals(name))
                .findFirst().orElse(null);
        if (dbInfo == null) {
            try {
                synchronized (listDbSource) {
                    dbInfo = getDbInfoByName(name);
                    setDbInfoByDefaultUrlTemplate(dbInfo);
                    jdbcDataBase = getNewJdbcDataBase(dbInfo);
                    listDbSource.put(dbInfo, jdbcDataBase);
                }
            } catch (Exception e) {
                throw new RuntimeException("获取JdbcDataBase异常!--" + e.getMessage());
            }

        } else {
            jdbcDataBase = listDbSource.get(dbInfo);
        }
        return jdbcDataBase;
    }

    /**
     * 通过sourceName获取配置信息对应的实例
     *
     * @param name
     * @return indi.cyh.jdbctool.modle.DbInfo
     * @author CYH
     * @date 2021/2/25 0025 9:32
     **/
    private static DbInfo getDbInfoByName(String name) {
        List<DbInfo> dbInfos = ConfigCenter.getDefalutDbInfos();
        for (DbInfo temp : dbInfos) {
            if (name.equals(temp.getSourceName())) {
                return temp;
            }
        }
        throw new RuntimeException("配置文件中没有配置数据库信息!");
    }

    /**
     * 过sourceName获取一个JdbcDataBase实例
     *
     * @param entity
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author cyh
     * 2020/7/16 21:10
     **/
    public static JdbcDataBase getJdbcDataBaseByInfo(DbInfo entity, boolean useDbTemplate) throws Exception {
        //是否需要使用模板信息
        if (useDbTemplate) {
            if (StringTool.isNotEmpty(entity.getType())) {
                //把配置结合参数转换为db连接实体类
                setDbInfoByDefaultUrlTemplate(entity);
            } else {
                throw new RuntimeException("db信息中缺少类型(type)");
            }
        } else if (StringTool.isEmpty(entity.getConnectStr())) {
            throw new RuntimeException("db信息中缺少连接字符串(connectStr)");
        }
        //根据实体生成数据源
        JdbcDataBase db = null;
        try {
            for (DbInfo info : listDbSource.keySet()) {
                if (DbInfo.equals(info, entity)) {
                    System.out.println("获取到已有连接池:" + info.getConnectStr());
                    System.out.println("目前连接池数量: " + listDbSource.size());
                    db = listDbSource.get(info);
                }
            }
        } catch (Exception e) {
            System.out.println("获取已有连接池时异常: " + e.getMessage());
        }
        if (db == null) {
            db = getNewJdbcDataBase(entity);
        }
        return db;
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
        for (DataBaseTemplate config : ConfigCenter.getDefaultDataBaseTemplateMap().values()) {
            if (entity.getType().equals(config.getType())) {
                setDbInfo(entity, config);
                return;
            }
        }
        throw new Exception("不支持的数据源类型!");
    }

    private static void setDbInfo(DbInfo entity, DataBaseTemplate config) {
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
    private static String getDbConnectUrl(DataBaseTemplate config, DbInfo entity) {
        String urlTemplate;
        if (StringTool.isEmpty(entity.getConnectStr())) {
            urlTemplate = config.getJdbcTemplate();
            urlTemplate = urlTemplate.replace(TemplateConfig.IP, entity.getIp());
            urlTemplate = urlTemplate.replace(TemplateConfig.PORT, String.valueOf(entity.getPort()));
            urlTemplate = urlTemplate.replace(TemplateConfig.END_PARAM, entity.getEndParam());
        } else {
            urlTemplate = entity.getConnectStr();
        }
        return urlTemplate;
    }

    /**
     * 生成新的连接池
     *
     * @param dbInfo
     * @return void
     * @author CYH
     * @date 2020/7/10 0010 15:49
     **/
    private static JdbcDataBase getNewJdbcDataBase(DbInfo dbInfo) {
        System.out.println("新增连接池-连接:  " + dbInfo.getConnectStr());
        DruidDataSource dataSource = ConfigCenter.getDefaultDataSource();
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
        if (ConfigCenter.isIsDebugger()) {
            System.out.println("目前连接池数量: " + listDbSource.size());
        }
        return newDb;
    }
}
