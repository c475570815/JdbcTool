package indi.cyh.jdbctool.event;


import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.main.JdbcDateBase;
import indi.cyh.jdbctool.modle.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * spring boot 配置环境事件监听
 */
@Component
public class EnvironmentPreparedEvent implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private DbConfig config;
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        DbInfo dbInfo = new DbInfo() {{
            setDriverClassName("com.mysql.cj.jdbc.Driver");
            setConnectStr("jdbc:mysql://127.0.0.1:3306/singlewood?serverTimezone=UTC");
            setDbType("mysql");
            setIp("127.0.0.1");
            setPort(3306);
            setLogoinName("root");
            setPwd("root");
            setEndParam("singlewood?serverTimezone=UTC");
        }};
        try {
            JdbcDateBase db = new JdbcDateBase(dbInfo,config);
            JdbcTemplate template = db.getJdbcTemplate();
            System.out.println(template.queryForObject("SELECT Count(*) FROM cp_card", int.class).toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}