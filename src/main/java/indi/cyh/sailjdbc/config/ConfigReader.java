package indi.cyh.sailjdbc.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.sailjdbc.tool.FileTool;
import indi.cyh.sailjdbc.tool.LogTool;

import java.io.InputStream;
import java.net.URL;

/**
 * 类功能描述: 配置文件读取
 *
 * @author mengcaiwen <2664266470@qq.com>
 * @version 1.0
 * @date 2021/2/23 9:59
 * @since jdk版本 1.8
 */
public class ConfigReader {

    private boolean isDebugger = true;

    private JSONArray dataBaseConfig = new JSONArray();

    private JSONArray dataBaseTemplate = new JSONArray();

    private JSONObject druidConfig = new JSONObject();

    private JSONObject logConfig = new JSONObject();

    public boolean isDebugger() {
        return isDebugger;
    }


    public JSONObject getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(JSONObject logConfig) {
        this.logConfig = logConfig;
    }

    public JSONArray getDataBaseConfig() {
        return dataBaseConfig;
    }

    public JSONArray getDataBaseTemplate() {
        return dataBaseTemplate;
    }

    public JSONObject getDruidConfig() {
        return druidConfig;
    }

    /**
     * 私有构造方法 防止外部不经过read方法直接构造读取
     */
    private ConfigReader() {

    }

    /**
     * 方法功能描述:
     *
     * @param configFileName 配置文件
     * @return void空
     */
    public static ConfigReader read(String configFileName) {
        ConfigReader reader = null;
        try {
            //获取 系统变量configFileName  没有就使用默认的wood.json
            URL fileUrl = ConfigReader.class.getClassLoader().getResource(configFileName);
            InputStream in = fileUrl.openStream();
            String configJsonString = FileTool.readToString(in);
            JSONObject config = JSONObject.parseObject(configJsonString);
            reader = new ConfigReader();
            if (config.containsKey("dataBaseConfig")) {
                reader.dataBaseConfig = config.getJSONArray("dataBaseConfig");
            }
            if (config.containsKey("dataBaseTemplate")) {
                reader.dataBaseTemplate = config.getJSONArray("dataBaseTemplate");
            }
            if (config.containsKey("druidConfig")) {
                reader.druidConfig = config.getJSONObject("druidConfig");
            }
            if (config.containsKey("logConfig")) {
                reader.logConfig = config.getJSONObject("logConfig");
            }

        } catch (Exception e) {
            LogTool.handleExceptionLog("读取配置文件时出错", true, e);
        }
        return reader;
    }
}
