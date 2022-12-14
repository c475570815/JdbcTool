package indi.cyh.jdbctool.config;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.modle.log.ConsoleLogConfig;
import indi.cyh.jdbctool.modle.log.FileLogConfig;
import indi.cyh.jdbctool.tool.FileUtils;
import indi.cyh.jdbctool.tool.LogTool;

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
        }
    }
}
