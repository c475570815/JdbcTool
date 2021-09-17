package indi.cyh.jdbctool.config;

import com.alibaba.druid.pool.DruidDataSource;
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

    private static boolean isDebugger = true;

    private static DruidDataSource defaultDataSource;

    final static Map<String, DataBaseTemplate> defaultDataBaseTemplateMap = new HashMap<>();

    static List<DbInfo> defalutDbInfos;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        try {
            //获取 系统变量configFileName  没有就使用默认的wood.json
            String configFileName = System.getProperty("configFile");
            LogTool.printLog("从JVM变量获取配置文件名:%s", configFileName);
            if (StringTool.isEmpty(configFileName)) {
                configFileName = System.getenv("JdbcToolConfigFile");
                LogTool.printLog("从环境变量获取配置文件名:%s", configFileName);
            }
            if (StringTool.isEmpty(configFileName)) {
                configFileName = "wood.json";
                LogTool.printLog("使用默认配置文件名:%s", configFileName);
            }
            ConfigReader reader = ConfigReader.read(configFileName);
            if (reader == null) {
                RuntimeException runtimeException = new RuntimeException("配置文件读取失败!");
                LogTool.printException("配置文件读取失败!", true, runtimeException);
                throw runtimeException;
            }
            //成功读取配置文件
            isDebugger = reader.isDebugger();
            //第一配置好默认的数据源对象
            defaultDataSource = DataSourceConfig.getDataSource(reader.getDruidConfig());
            //第二配置好 模板信息
            TemplateConfig.getDataBaseTemplate(defaultDataBaseTemplateMap, reader.getDataBaseTemplate());
            //第三配置好 数据库信息
            defalutDbInfos = DataBaseInfoConfig.getDataBaseInfo(reader.getDataBaseConfig());
        } catch (Exception e) {
            LogTool.printException("读取配置文件时出错", true, e);
        }
    }


    public static boolean isIsDebugger() {
        return isDebugger;
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
}
