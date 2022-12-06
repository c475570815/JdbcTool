package indi.cyh.sailjdbc.modle.log;

/**
 * 控制台日志输出配置
 *
 * @Description TODO
 * @Author CYH
 * @Date 2021/9/17 15:13
 **/
public class ConsoleLogConfig {

    /**
     * 是否开启日志记录
     */
    private static boolean enable = true;

    public static boolean isEnable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        ConsoleLogConfig.enable = enable;
    }
}
