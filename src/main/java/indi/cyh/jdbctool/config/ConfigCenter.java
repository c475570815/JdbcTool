package indi.cyh.jdbctool.config;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;
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

    static Map<String, DataBaseTemplate> defaultDataBaseTemplateMap;

    static List<DbInfo> defalutDbInfos;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        try {
            //获取 系统变量configFileName  没有就使用默认的wood.json
            String configFileName = System.getProperty("configFile");
            ConfigReader reader = ConfigReader.read(configFileName);
            if (reader==null){
                throw new RuntimeException("config file read error......");
            }
            //成功读取配置文件
            isDebugger=reader.isDebugger();
            //第一配置好默认的数据源对象
            defaultDataSource=DataSourceConfig.getDataSource(reader.getDruidConfig());
            //第二配置好 模板信息
            defaultDataBaseTemplateMap=TemplateConfig.getDataBaseTemplate(reader.getDataBaseTemplate());
            //第三配置好 数据库信息
            defalutDbInfos=DataBaseInfoConfig.getDataBaseInfo(reader.getDataBaseConfig());
        } catch (Exception e) {
            System.out.println("读取配置文件时出错!");
            e.printStackTrace();
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
}
