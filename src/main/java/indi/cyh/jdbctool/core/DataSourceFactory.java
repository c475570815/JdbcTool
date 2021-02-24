package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.config.ConfigCenter;
import indi.cyh.jdbctool.config.TemplateConfig;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.tool.JdbcUrlTool;
import indi.cyh.jdbctool.tool.LogTool;
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
    /**
     * 静态连接池  name JdbcDataBase
     */
    private static final ConcurrentHashMap<String, JdbcDataBase> listDbSource = new ConcurrentHashMap<>();

    public static JdbcDataBase getJdbcDataBase(){
       return getJdbcDataBase("mainDb");
    }

    public static JdbcDataBase getJdbcDataBase(String name){
        JdbcDataBase jdbcDataBase = listDbSource.get(name);
        if (jdbcDataBase==null){
            synchronized (listDbSource){
                jdbcDataBase = listDbSource.get(name);
                if (jdbcDataBase==null){
                    jdbcDataBase=createJdbcDataBase(name);
                    if (jdbcDataBase!=null){
                        listDbSource.put(name,jdbcDataBase);
                    }
                }
            }
        }
        return jdbcDataBase;
    }


    private static JdbcDataBase createJdbcDataBase(String name){
        JdbcDataBase jdbcDataBase=null;
        try {
            DbInfo dbInfo=null;
            List<DbInfo> dbInfos = ConfigCenter.getDefalutDbInfos();
            for (DbInfo temp : dbInfos) {
                if (name.equals(temp.getSourceName())){
                    dbInfo=temp;
                    break;
                }
            }
            if (dbInfo==null){
                throw new RuntimeException("配置文件中没有配置数据库信息!");
            }
            setDbInfoByDefaultUrlTemplate(dbInfo);
            jdbcDataBase = getNewJdbcBase(dbInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jdbcDataBase;
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
    private static JdbcDataBase getNewJdbcBase(DbInfo dbInfo) {
        System.out.println("新增连接池-连接:  " + dbInfo.getConnectStr());
        DruidDataSource dataSource=ConfigCenter.getDefaultDataSource();
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
        if (ConfigCenter.isIsDebugger()){
            System.out.println("目前连接池数量: " + listDbSource.size());
        }
        return newDb;
    }
}
