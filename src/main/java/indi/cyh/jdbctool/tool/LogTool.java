package indi.cyh.jdbctool.tool;

import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.core.JdbcDataBase;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.log.ConsoleLogConfig;
import indi.cyh.jdbctool.modle.log.FileLogConfig;
import org.springframework.lang.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @ClassName LogTool
 * @Description TODO
 * @Author cyh
 * @Date 2020/7/14 0014 16:44
 */
public class LogTool {

    /**
     * 普通日志分割字符串
     */
    private static String LOG_SEPARATOR = "##########################################JDBCTOOL-LOG##########################################\n";
    /**
     * 异常日志分割字符串
     */
    private static String EXCEPTION_LOG_SEPARATOR = "##########################################JDBCTOOL-EXCEPTION-LOG##########################################\n";


    private static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理日志-公共部分
     *
     * @param log
     * @return void
     * @author CYH
     * @date 2021/9/17 16:44
     **/
    private static void handleLogCommon(String log, boolean addTime) {
        Date now = new Date();
        if (addTime) {
            //加时间
            log = String.format("Jdbctool-%s:%s%n", timeFormat.format(now), log);
        }
        if (ConsoleLogConfig.isEnable()) {
            System.out.printf(log);
        }
        if (FileLogConfig.isEnable()) {
            String path = String.format("%s%s%s-%tF.log",
                    FileLogConfig.getToolLogPath(),
                    File.separator,
                    FileLogConfig.TOOL_LOG_NAME,
                    now);
            FileUtils.fileLinesWrite(path, log, true);
        }
    }

    /**
     * 处理异常日志-公共部分
     *
     * @param log
     * @param e
     * @param isPrintStackTrace
     * @return void
     * @author CYH
     * @date 2021/9/17 16:58
     **/
    private static void handleExceptionLogCommon(String log, Exception e, boolean isPrintStackTrace, boolean addTime) {
        Date now = new Date();
        if (addTime) {
            //加时间
            log = String.format("Jdbctool-%s:%s%n", timeFormat.format(now), log);
        }
        System.out.println(log);
        if (FileLogConfig.isEnable()) {
            String path = String.format("%s%s%s-%tF.log",
                    FileLogConfig.getExceptionLogPath(),
                    File.separator,
                    FileLogConfig.TOOL_LOG_NAME,
                    now);
            FileUtils.fileLinesWrite(path, log, true);
        }
        if (isPrintStackTrace) {
            e.printStackTrace();
        }
    }


    /**
     * sql 打印
     *
     * @param sql    执行sql
     * @param params 参数
     * @return void
     * @author CYH
     * @date 2020/7/14 0014 16:47
     **/
    public void handleSqlLog(String sql, String jdbcUrl, @Nullable Object... params) {
        try {
            //默认打开打印  当配置中设置了非调试模式则关闭打印
            if (ConsoleLogConfig.isEnable() || FileLogConfig.isEnable()) {
                String sqlLog = getSqlLog(sql, jdbcUrl, params);
                handleLogCommon(sqlLog, false);
            }
        } catch (Exception e) {
            handleExceptionLog("sql日志处理异常", false, e);
        }
    }

    /**
     * 获取sql日志
     *
     * @param sql
     * @param jdbcUrl
     * @param params
     * @return java.lang.String
     * @author CYH
     * @date 2021/9/17 16:58
     **/
    private String getSqlLog(String sql, String jdbcUrl, Object[] params) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(LOG_SEPARATOR);
        buffer.append("conStr:").append(jdbcUrl).append("\n");
        buffer.append(String.format("time:%s\n", timeFormat.format(new Date())));
        buffer.append("\n");
        buffer.append("sql : ").append(sql).append("\n");
        if (params != null && params.length != 0) {
            buffer.append("\n");
            buffer.append(params.length).append("  params").append("\n");
            buffer.append("\n");
            for (int i = 0; i < params.length; i++) {
                try {
                    buffer.append("param-").append(i + 1).append(": ").append(params[i]).append("[").append(params[i] == null ? "null" : params[i].getClass().getName()).append("]\n");
                } catch (Exception e) {
                    buffer.append("param-").append(i + 1).append(": unKnowParam\n");
                }
                buffer.append("\n");
            }
        }
        buffer.append(LOG_SEPARATOR);
        return buffer.toString();
    }

    /**
     * 获取消耗时间日志字符串
     *
     * @param start
     * @return java.lang.String
     * @author CYH
     * @date 2021/9/17 16:41
     **/
    private String getTimeLostLogStr(long start) {
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
        return String.format("执行耗时:%s\n", builder);
    }


    /**
     * sql执行耗时
     *
     * @param start 执行前的时间毫秒数
     * @return void
     * @author CYH
     * @date 2020/7/14 0014 16:46
     **/
    public void handleTimeLost(long start) {
        if (ConsoleLogConfig.isEnable() || FileLogConfig.isEnable()) {
            String timeLostLog = getTimeLostLogStr(start);
            handleLogCommon(timeLostLog, true);
        }
    }

    /**
     * 处理普通日志
     *
     * @param log
     * @param params
     * @return void
     * @author CYH
     * @date 2021/9/17 17:00
     **/
    public static void handleLog(String log, @Nullable Object... params) {
        try {
            if (ConsoleLogConfig.isEnable() || FileLogConfig.isEnable()) {
                handleLogCommon(String.format(log, params), true);
            }
        } catch (Exception e) {
            handleExceptionLog("日志处理异常", false, e);
        }
    }

    /**
     * 处理异常日志
     *
     * @param logPre
     * @param isPrintStackTrace
     * @param e
     * @param params
     * @return void
     * @author CYH
     * @date 2021/9/17 17:01
     **/
    public static void handleExceptionLog(String logPre, boolean isPrintStackTrace, Exception e, @Nullable Object... params) {
        if (ConsoleLogConfig.isEnable() || FileLogConfig.isEnable()) {
            String log = "";
            logPre = String.format(logPre, params);
            log += EXCEPTION_LOG_SEPARATOR;
            log += String.format("time:%s\n", timeFormat.format(new Date()));
            log += String.format("%s:%n%s%n", logPre, e.getMessage());
            log += EXCEPTION_LOG_SEPARATOR;
            handleExceptionLogCommon(log, e, isPrintStackTrace, false);
            if (isPrintStackTrace) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理异常日志 带db信息
     *
     * @param db
     * @param logPre
     * @param isPrintStackTrace
     * @param e
     * @param params
     * @return void
     * @author CYH
     * @date 2021/9/17 17:01
     **/
    public static void handleExceptionLogByDbInfo(JdbcDataBase db, String logPre, boolean isPrintStackTrace, Exception e, @Nullable Object... params) {
        if (ConsoleLogConfig.isEnable() || FileLogConfig.isEnable()) {
            String log = "";
            logPre = String.format(logPre, params);
            DbInfo info = DataSourceFactory.getDbInfoByJdbcDataBase(db);
            log += EXCEPTION_LOG_SEPARATOR;
            log += String.format("time:%s\n", timeFormat.format(new Date()));
            log += String.format("%s:%n出错连接%s---用户名:%s%n", logPre, info.getConnectStr(), info.getLoginName());
            log += String.format("%s%n", e.getMessage());
            log += EXCEPTION_LOG_SEPARATOR;
            handleExceptionLogCommon(log, e, isPrintStackTrace, false);
            if (isPrintStackTrace) {
                e.printStackTrace();
            }
        }
    }
}
