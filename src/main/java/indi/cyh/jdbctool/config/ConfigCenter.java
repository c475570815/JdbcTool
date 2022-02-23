package indi.cyh.jdbctool.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.tool.LogTool;
import indi.cyh.jdbctool.tool.StringTool;

import java.util.*;

/**
 * 类功能描述: 配置中心所有配置配置好后创建的对象信息,在类被加载到内存的时候被初始化
 *
 * @author mengcaiwen
 * @version 1.0
 * @date 2021/2/23 14:50
 * @since jdk版本 1.8
 */
public class ConfigCenter {

    /**
     * 配置文件全名
     */
    public static String  configFileName;

    /**
     * druid数据源模板
     */
    private static DruidDataSource defaultDataSource;

    /**
     * jdbc模板信息
     */
    final static Map<String, DataBaseTemplate> defaultDataBaseTemplateMap = new HashMap<>();

    /**
     * jdbc数据库连接信息
     */
    static List<DbInfo> defalutDbInfos;

    static {
        loadConfig();
    }

    public static DruidDataSource getDefaultDataSource() {
        return defaultDataSource.cloneDruidDataSource();
    }

    public static Map<String, DataBaseTemplate> getDefaultDataBaseTemplateMap() {
        return defaultDataBaseTemplateMap;
    }

    public static List<DbInfo> getDefalutDbInfos() {
        return defalutDbInfos;
    }


    public static void loadConfig() {
        try {
            //获取 系统变量configFileName  没有就使用默认的wood.json
            configFileName = System.getProperty("configFile");
            LogTool.handleLog("从JVM变量获取配置文件名:%s", configFileName);
            if (StringTool.isEmpty(configFileName)) {
                configFileName = System.getenv("JdbcToolConfigFile");
                LogTool.handleLog("从环境变量获取配置文件名:%s", configFileName);
            }
            if (StringTool.isEmpty(configFileName)) {
                configFileName = "wood.json";
                LogTool.handleLog("使用默认配置文件名:%s", configFileName);
            }
            ConfigReader reader = ConfigReader.read(configFileName);
            if (reader == null) {
                RuntimeException runtimeException = new RuntimeException("配置文件读取失败!");
                LogTool.handleExceptionLog("配置文件读取失败!", true, runtimeException);
                throw runtimeException;
            }
            //生成 日志配置信息
            LogTool.handleLog("读取日志配置");
            LogConfig.setConfigs(reader.getLogConfig());
            //生成默认的数据源对象
            LogTool.handleLog("读取druid配置");
            defaultDataSource = DataSourceConfig.getDataSource(reader.getDruidConfig());
            //生成 模板信息
            LogTool.handleLog("读取jdbc模板配置");
            TemplateConfig.getDataBaseTemplate(defaultDataBaseTemplateMap, reader.getDataBaseTemplate());
            //生成 数据库信息
            LogTool.handleLog("读取配置的数据库信息");
            defalutDbInfos = DataBaseInfoConfig.getDataBaseInfo(reader.getDataBaseConfig());
        } catch (Exception e) {
            LogTool.handleExceptionLog("读取配置文件时出错", true, e);
        }
    }

    /**
     * 添加数据库模板信息
     *
     * @param template
     * @return void
     * @author CYH
     * @date 2021/2/25 0025 9:51
     **/
    public static void addDataBaseTemplate(DataBaseTemplate template) {
        synchronized (defaultDataBaseTemplateMap) {
            TemplateConfig.addDataBaseTemplate(defaultDataBaseTemplateMap, template);
        }
    }

    /**
     * 判断JSONObject是否是空
     *
     * @param object
     * @return boolean
     * @author CYH
     * @date 2021/9/17 15:42
     **/
    public static boolean isJsonObjectNotNull(JSONObject object) {
        return object != null && object.keySet().size() != 0;
    }
}
