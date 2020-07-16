package indi.cyh.jdbctool.modle;

/**
 * @author gm
 * @className DbInfo
 * @description 数据库信息
 * @date 2020/4/11 8:41
 */
public class DbInfo {
    String dbType;
    String ip;
    Integer port;
    String endParam;
    String logoinName;
    String pwd;
    String connectStr;
    String driverClassName;


    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getConnectStr() {
        return connectStr;
    }

    public void setConnectStr(String connectStr) {
        this.connectStr = connectStr;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getEndParam() {
        return endParam;
    }

    public void setEndParam(String endParam) {
        this.endParam = endParam;
    }

    public String getLogoinName() {
        return logoinName;
    }

    public void setLogoinName(String logoinName) {
        this.logoinName = logoinName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public static boolean equals(DbInfo first, DbInfo secend) {
        return first.getConnectStr().equals(secend.getConnectStr()) && first.getLogoinName().equals(secend.getLogoinName()) && first.getPwd().equals(secend.getPwd());
    }
}
