package indi.cyh.jdbctool.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName JdbcUrlUtil
 * @Description TODO
 * @Author cyh
 * @Date 2020/7/8 0008 14:43
 */
public class JdbcUrlTool {
    static Pattern patternDbIp = Pattern.compile("(?<=//).*(?=:)");
    static Pattern patternDbType = Pattern.compile("(?<=:).[a-zA-Z]*(?=:)");
    static Pattern patternDbPort = Pattern.compile("(?<=\\d:).*(?=/)");
    static Pattern patternDbName = Pattern.compile("(?<=\\d/).*");

    public static Map<String, Object> findDataInfoMapByUrl(String jdbcUrl) {
        Map<String, Object> infoMap = new HashMap<>();
        //ip
        Matcher mDbIp = patternDbIp.matcher(jdbcUrl);
        mDbIp.find();
        infoMap.put("ip", mDbIp.group());

        //port
        Matcher mDbPort = patternDbPort.matcher(jdbcUrl);
        mDbPort.find();
        infoMap.put("port", mDbPort.group());

        //Dbtype
        Matcher mDbType = patternDbType.matcher(jdbcUrl);
        mDbType.find();
        infoMap.put("type", mDbType.group());

        //dbName
        Matcher mDbName = patternDbName.matcher(jdbcUrl);
        mDbName.find();
        infoMap.put("name", mDbName.group());
        return infoMap;
    }
}
