package indi.cyh.jdbctool.modle;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author gm
 * @className DbInfo
 * @description 数据库信息
 * @date 2020/4/11 8:41
 */
public class DbInfo {
    String sourceName;
    String type;
    String ip;
    Integer port;
    String endParam;
    String loginName;
    String pwd;
    String connectStr;
    String driverClassName;
    //秒
    int queryTimeOut = -1;
    DruidDataSource druidDataSource;

    // 重写hashcode方法
    @Override
    public int hashCode() {
        int result = connectStr.hashCode();
        result = 17 * result + loginName.hashCode();
        return result;
    }

    // 重写equals方法
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DbInfo)) {
            return false;
        }
        DbInfo dbInfo = (DbInfo) obj;
        // 地址相等
        if (this == dbInfo) {
            return true;
        }
        return dbInfo.connectStr.equals(this.connectStr) && dbInfo.loginName.equals(this.loginName);
    }

    public DbInfo clone() {
        DbInfo info = new DbInfo();
        info.setSourceName(this.sourceName);
        info.setType(this.type);
        info.setIp(this.ip);
        info.setPort(this.port);
        info.setEndParam(this.endParam);
        info.setLoginName(this.loginName);
        info.setPwd(this.pwd);
        info.setConnectStr(this.connectStr);
        info.setDriverClassName(this.driverClassName);
        info.setDruidDataSource(this.druidDataSource);
        return info;
    }

    public DruidDataSource getDruidDataSource() {
        return druidDataSource;
    }

    public int getQueryTimeOut() {
        return queryTimeOut;
    }

    public void setQueryTimeOut(int queryTimeOut) {
        this.queryTimeOut = queryTimeOut;
    }

    public void setDruidDataSource(DruidDataSource druidDataSource) {
        this.druidDataSource = druidDataSource;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
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
        return first.getConnectStr().equals(secend.getConnectStr()) && first.getLoginName().equals(secend.getLoginName()) && first.getPwd().equals(secend.getPwd());
    }
}
