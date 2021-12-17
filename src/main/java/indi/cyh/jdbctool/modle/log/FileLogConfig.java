package indi.cyh.jdbctool.modle.log;

/**
 * 控制台日志输出配置
 *
 * @Description TODO
 * @Author CYH
 * @Date 2021/9/17 15:13
 **/
public class FileLogConfig {

    /**
     * 是否开启日志记录
     */
    private static boolean enable = false;


    /**
     * 运行日志路径
     */
    public static String toolLogPath;

    /**
     * 运行日志名
     */
    public static String TOOL_LOG_NAME = "jdbctool-run-log";

    /**
     * 异常日志路径
     */
    public static String exceptionLogPath;


    /**
     * 异常日志名
     */
    public static String EXCEPTION_LOG_NAME = "jdbctool-exception-log";

    public static boolean isEnable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        FileLogConfig.enable = enable;
    }

    public static String getToolLogPath() {
        return toolLogPath;
    }

    public static void setToolLogPath(String toolLogPath) {
        FileLogConfig.toolLogPath = toolLogPath;
    }

    public static String getExceptionLogPath() {
        return exceptionLogPath;
    }

    public static void setExceptionLogPath(String exceptionLogPath) {
        FileLogConfig.exceptionLogPath = exceptionLogPath;
    }
}
