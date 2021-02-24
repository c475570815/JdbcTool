package indi.cyh.jdbctool.modle;

/**
 * @ClassName DataBaseTemplate
 * @Description TODO
 * @Author gm
 * @Date 2020/7/16 21:39
 */
public class DataBaseTemplate {

    private String jdbcTemplate;

    private String driverClassName;

    private int port;

    private String type;

    public String getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(String jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
