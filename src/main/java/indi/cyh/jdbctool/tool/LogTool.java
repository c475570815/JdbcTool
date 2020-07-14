package indi.cyh.jdbctool.tool;

import org.springframework.lang.Nullable;

/**
 * @ClassName LogTool
 * @Description TODO
 * @Author cyh
 * @Date 2020/7/14 0014 16:44
 */
public class LogTool {

    private Boolean debugger;

    public LogTool(Boolean isDebugger) {
        this.debugger = isDebugger;
    }


    /**
     * sql执行耗时
     * @param start  执行前的时间毫秒数
     * @return void
     * @author CYH
     * @date 2020/7/14 0014 16:46
     **/
    public void printTimeLost(long start) {
        if(debugger) {
            long l = System.currentTimeMillis() - start;
            StringBuilder builder = new StringBuilder();
            long millis = 1;
            long seconds = 1000 * millis;
            long minutes = 60 * seconds;
            long hours = 60 * minutes;
            long days = 24 * hours;
            if (l / days >= 1)
                builder.append((int) (l / days)).append("天");
            if (l % days / hours >= 1)
                builder.append((int) (l % days / hours)).append("小时");
            if (l % days % hours / minutes >= 1)
                builder.append((int) (l % days % hours / minutes)).append("分钟");
            if (l % days % hours % minutes / seconds >= 1)
                builder.append((int) (l % days % hours % minutes / seconds)).append("秒");
            long ms = l % days % hours % minutes % seconds / millis;
            if (ms >= 1)
                builder.append((int) (ms)).append("毫秒");
            System.out.println("耗时:" + builder.toString());
        }
    }
    /**
     * sql 打印
     * @param sql  执行sql
     * @param params 参数
     * @return void
     * @author CYH
     * @date 2020/7/14 0014 16:47
     **/
    public   void printLog(String sql,String jdbcUrl, @Nullable Object... params) {
        //默认打开打印  当配置中设置了非调试模式则关闭打印
        if (debugger) {
            System.out.println("##########################################JDBCTOOL##########################################");
            System.out.println("conStr:" +jdbcUrl);
            System.out.println("");
            System.out.println("sql : " + sql);
            if (params != null && params.length != 0) {
                System.out.println("");
                System.out.println(params.length + "  params");
                System.out.println("");
                for (int i = 0; i < params.length; i++) {
                    System.out.println("param-" + (i + 1) + ": " + String.valueOf(params[i]) + "[" + params[i].getClass().getName() + "]");
                    System.out.println("");
                }
            }
            System.out.println("##########################################JDBCTOOL##########################################");
        }
    }
}
