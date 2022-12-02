package indi.cyh.jdbctool.config;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.modle.log.ConsoleLogConfig;
import indi.cyh.jdbctool.modle.log.TimeoutConfig;
import indi.cyh.jdbctool.modle.log.FileLogConfig;
import indi.cyh.jdbctool.tool.FileUtils;

/**
 * 日志配置中心
 *
 * @Description TODO
 * @Author CYH
 * @Date 2021/9/17 15:01
 **/
public class LogConfig {
    /**
     * 默认日志路径
     */
    public static String DEFAULT_BASE_PATH = FileUtils.getConTextPath();


    public static void setConfigs(JSONObject logConfig) {
        if (ConfigCenter.isJsonObjectNotNull(logConfig)) {
            //文件配置
            JSONObject fileConfig = logConfig.getJSONObject("file");
            //控制台配置
            JSONObject consoleConfig = logConfig.getJSONObject("console");
            //异常超时记录
            JSONObject timeoutConfig = logConfig.getJSONObject("timeout");
            if (ConfigCenter.isJsonObjectNotNull(consoleConfig)) {
                ConsoleLogConfig.setEnable(consoleConfig.getBoolean("enable"));
            }
            if (ConfigCenter.isJsonObjectNotNull(fileConfig)) {
                boolean enable = fileConfig.getBoolean("enable");
                FileLogConfig.setEnable(enable);
                if (enable) {
                    String exceptionLogPath = fileConfig.getString("exceptionLogPath");
                    String toolLogPath = fileConfig.getString("toolLogPath");
                    FileLogConfig.setExceptionLogPath(StringUtils.isEmpty(exceptionLogPath) ? DEFAULT_BASE_PATH : exceptionLogPath);
                    FileLogConfig.setToolLogPath(StringUtils.isEmpty(toolLogPath) ? DEFAULT_BASE_PATH : toolLogPath);
                }
            }
            if (ConfigCenter.isJsonObjectNotNull(timeoutConfig)) {
                boolean enable = timeoutConfig.getBoolean("enable");
                TimeoutConfig.setEnable(enable);
                TimeoutConfig.setMaxLostTime(timeoutConfig.getFloat("maxLostTime"));
                if (enable) {
                    String filePath = timeoutConfig.getString("filePath");
                    String fileName = timeoutConfig.getString("fileName");
                    TimeoutConfig.setFilePath(StringUtils.isEmpty(filePath) ? DEFAULT_BASE_PATH : filePath);
                    if (!StringUtils.isEmpty(fileName)) {
                        TimeoutConfig.setFileName(fileName);
                    }
                }
            }
        }
    }
}
