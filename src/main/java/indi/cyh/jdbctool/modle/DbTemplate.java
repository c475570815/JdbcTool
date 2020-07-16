package indi.cyh.jdbctool.modle;

/**
 * @ClassName DbTemplate
 * @Description TODO
 * @Author gm
 * @Date 2020/7/16 21:39
 */
public class DbTemplate {
    String urlTemplate;
    String driverClassName;
    int port;
    String dbType;

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}
