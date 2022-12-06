package indi.cyh.sailjdbc.modle.log;

/**
 * 异常和超时日志配置
 *
 * @Description TODO
 * @Author CYH
 * @Date 2021/9/17 15:13
 **/
public class TimeoutConfig {

    /**
     * 是否开启日志记录
     */
    private static boolean enable = true;

    private static float maxLostTime;


    private static String filePath;

    private static String fileName = "jdbctool-timeout-log";


    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        TimeoutConfig.filePath = filePath;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        TimeoutConfig.fileName = fileName;
    }

    public static float getMaxLostTime() {
        return maxLostTime;
    }

    public static void setMaxLostTime(float maxLostTime) {
        TimeoutConfig.maxLostTime = maxLostTime;
    }

    public static boolean isEnable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        TimeoutConfig.enable = enable;
    }
}
