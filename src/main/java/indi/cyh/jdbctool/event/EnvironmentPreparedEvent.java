package indi.cyh.jdbctool.event;


import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.main.JdbcDateBase;
import indi.cyh.jdbctool.modle.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * spring boot 配置环境事件监听
 */
@Component
public class EnvironmentPreparedEvent implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private DbConfig config;

    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        for (int i = 1; i <= 50; i++) {
            System.out.println("");
            System.out.println("thread-" + i + "    start!");
            System.out.println("");
            final String threadNumber = String.valueOf(i);
            new Thread(() -> {
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
                    JdbcDateBase db = new JdbcDateBase(dbInfo, config);
                    System.out.println("");
                    System.out.println(db.queryOneRow("SELECT Count(*) FROM cp_card", int.class));
                    System.out.println("thread-" + threadNumber + "end");
                    System.out.println("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

}