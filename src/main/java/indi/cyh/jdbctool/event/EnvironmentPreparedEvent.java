package indi.cyh.jdbctool.event;


import indi.cyh.jdbctool.entity.StParm;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.main.JdbcDateBase;
import indi.cyh.jdbctool.modle.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * spring boot 配置环境事件监听
 */
@Component
public class EnvironmentPreparedEvent implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private DbConfig config;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        DbInfo dbInfo = new DbInfo() {{
            setDbType("oracle");
            setIp("10.1.32.231");
            setPort(1521);
            setLogoinName("zw_gwxt");
            setPwd("1");
            setEndParam("ORCL");
        }};
        try {
            JdbcDateBase db = new JdbcDateBase(dbInfo, config);
            List<StParm>  list=db.queryList("SELECT * FROM ST_PARM", StParm.class);
            System.out.println(list.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}