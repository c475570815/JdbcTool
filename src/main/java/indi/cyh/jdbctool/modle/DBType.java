package indi.cyh.jdbctool.modle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库类型
 *
 * @param null
 * @author CYH
 * @return
 * @date 2022/11/29 10:55
 **/
public enum DBType {

    MYSQL,
    SQLSERVER,
    ORACLE,
    POSTGRESQL;

    private String value;

    public final static Map<DBType, List<String>> driverClassList;

    static {
        driverClassList = new HashMap<>();
        driverClassList.put(DBType.MYSQL, new ArrayList<String>() {{
            add("com.mysql.jdbc.Driver");
            add("com.mysql.cj.jdbc.Driver");
        }});
        driverClassList.put(DBType.SQLSERVER, new ArrayList<String>() {{
            add("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }});
        driverClassList.put(DBType.ORACLE, new ArrayList<String>() {{
            add("oracle.jdbc.driver.OracleDriver");
            add("oracle.jdbc.OracleDriver");
        }});
        driverClassList.put(DBType.POSTGRESQL, new ArrayList<String>() {{
            add("org.postgresql.Driver");
        }});
    }

    public static DBType getDbTypeByDriverClassName(String driverClassName) {
        for (DBType dbType : driverClassList.keySet()) {
            if (driverClassList.get(dbType).contains(driverClassName)) {
                return dbType;
            }
        }
        throw new RuntimeException(String.format("未知的驱动类型:%s", driverClassName));
    }


}
