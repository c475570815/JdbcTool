package indi.cyh.jdbctool.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.DbTemplate;
import indi.cyh.jdbctool.tool.FileTool;
import indi.cyh.jdbctool.tool.StringTool;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName DbConfig
 * @Description TODO
 * @Author gm
 * @Date 2020/4/11 8:54
 */
public class DbConfig {

    //jdbc连接生成相关参数
    public static final String IP = "{{IP}}";
    public static final String PORT = "{{PORT}}";
    public static final String END_PARAM = "{{END_PARAM}}";


    //静态控制锁
    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl;
    private static final ReentrantReadWriteLock.WriteLock wl;

    static Map<String, DbTemplate> defaultUrlTemplateMap;
    static boolean isDebugger = true;
    static List<DbInfo> defalutDatasource;

    static {
        //锁赋值
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
    }

    public static void loadConfig() {
        try {
            //获取 系统变量configFileName  没有就使用默认的wood.json
            String targetFileName = System.getProperty("configFile");
            String fileName = targetFileName == null ? "wood.json" : targetFileName;
            fileName = StringTool.isEmpty(fileName) ? "wood.json" : fileName;
            String jsonConfig = getConfigFromConfigFile(fileName);
            JSONObject config = JSONObject.parseObject(jsonConfig);
            //加载默认模板
            loadDefaultUrlTemplate();
            setValueFromConfig(config);
        } catch (Exception e) {
            System.out.println("读取配置文件时出错!");
            e.printStackTrace();
        }
    }

    /**
     * 根据配置文件设置值
     *
     * @param config
     * @return void
     * @author cyh
     * 2020/12/8 18:56
     **/
    private static void setValueFromConfig(JSONObject config) throws Exception {
        isDebugger = config.get("isDebugger").equals(true);
        //加载通过配置问价添加的模板
        List<DbTemplate> dbTemplates = getDbTemplateList(config.getJSONObject("dbConfig").getJSONArray("templateList"));

        for (DbTemplate dbTemplate : dbTemplates) {
            if (!defaultUrlTemplateMap.keySet().contains(dbTemplate.getType())) {
                addDbTmplate(dbTemplate);
            }
        }
        defalutDatasource = getdefalutDatasource(config.getJSONObject("dbConfig").getJSONArray("datasource"));


        DataSourceConfig.loadConfig(config.getJSONObject("druid"));
        DataSourceFactory.loadMainDbConfig();
    }

    private static List<DbInfo> getdefalutDatasource(JSONArray jsonArray) {
        List<DbInfo> res = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            DbInfo dbInfo = new DbInfo();
            dbInfo.setSourceName(object.getString("jdbcTemplate"));
            dbInfo.setType(object.getString("type"));
            dbInfo.setIp(object.getString("ip"));
            dbInfo.setPort(object.getInteger("port"));
            dbInfo.setLoginName(object.getString("loginName"));
            dbInfo.setPwd(object.getString("pwd"));
            dbInfo.setEndParam(object.getString("endParam"));
            res.add(dbInfo);
        }
        return res;
    }

    private static List<DbTemplate> getDbTemplateList(JSONArray jsonArray) {
        List<DbTemplate> res = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            DbTemplate template = new DbTemplate();
            template.setJdbcTemplate(object.getString("jdbcTemplate"));
            template.setType(object.getString("type"));
            template.setPort(object.getInteger("port"));
            template.setDriverClassName(object.getString("driverClassName"));
            res.add(template);
        }
        return res;
    }

    /**
     * 从配置文件获取配置的json
     *
     * @param fileName
     * @return java.lang.String
     * @author CYH
     * @date 2020/12/8 0008 10:29
     **/
    private static String getConfigFromConfigFile(String fileName) throws IOException {
        URL fileUrl = DbConfig.class.getClassLoader().getResource(fileName);
        System.out.println("fileUrl = " + fileUrl.getPath());
        InputStream in = fileUrl.openStream();
        return FileTool.readToString(in);
    }

    public static Map<String, DbTemplate> getDefaultUrlTemplateMap() {
        return defaultUrlTemplateMap;
    }

    public static void setDefaultUrlTemplateMap(Map<String, DbTemplate> defaultUrlTemplateMap) {
        DbConfig.defaultUrlTemplateMap = defaultUrlTemplateMap;
    }

    public static boolean isIsDebugger() {
        return isDebugger;
    }

    public static void setIsDebugger(boolean isDebugger) {
        DbConfig.isDebugger = isDebugger;
    }

    public static List<DbInfo> getDefalutDatasource() {
        return defalutDatasource;
    }

    public static void setDefalutDatasource(List<DbInfo> defalutDatasource) {
        DbConfig.defalutDatasource = defalutDatasource;
    }

    /**
     * 配置默认jdbc-url模板
     *
     * @param
     * @return void
     * @author cyh
     * 2020/7/16 20:58
     **/
    private static void loadDefaultUrlTemplate() {
        defaultUrlTemplateMap = new HashMap<>();
        DbTemplate mysql = new DbTemplate() {{
            setJdbcTemplate("jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(3306);
            setDriverClassName("com.mysql.cj.jdbc.Driver");
            setType("mysql");
        }};
        DbTemplate oracle = new DbTemplate() {{
            setJdbcTemplate("jdbc:oracle:thin:@{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(1521);
            setDriverClassName("oracle.jdbc.driver.OracleDriver");
            setType("oracle");
        }};
        DbTemplate postgres = new DbTemplate() {{
            setJdbcTemplate("jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(5432);
            setDriverClassName("org.postgresql.Driver");
            setType("postgres");
        }};
        defaultUrlTemplateMap.put("mysql", mysql);
        defaultUrlTemplateMap.put("oracle", oracle);
        defaultUrlTemplateMap.put("postgres", postgres);
    }

    /**
     * 添加模板
     *
     * @param
     * @return void
     * @author cyh
     * 2020/7/16 21:49
     **/
    public static void addDbTmplate(DbTemplate template) throws Exception {
        wl.lock();
        try {
            if (template.getJdbcTemplate().contains(IP) && template.getJdbcTemplate().contains(PORT) && template.getJdbcTemplate().contains(END_PARAM)) {
                boolean isExist = defaultUrlTemplateMap.keySet().contains(template.getType());
                defaultUrlTemplateMap.put(template.getType(), template);
                if (isExist) {
                    System.out.println(template.getType() + "模板配置已被覆盖!");
                }
            } else {
                throw new Exception("模板必须包含{{IP}}、{{PORT}}、{{END_PARAM}}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wl.unlock();
        }
    }
}
