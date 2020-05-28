package indi.cyh.jdbctool.event;


import indi.cyh.jdbctool.entity.BsDiary;
import indi.cyh.jdbctool.entity.StParm;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.main.JdbcDateBase;
import indi.cyh.jdbctool.modle.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;
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
            setDbType("mysql");
            setIp("*.*.*.*");
            setPort(3306);
            setLogoinName("***");
            setPwd("***");
            setEndParam("singlewood");
        }};
        try {
            JdbcDateBase db = new JdbcDateBase(dbInfo, config);
//            List<BsDiary>  list=db.queryList("SELECT * FROM bs_diary ", BsDiary.class);
//            System.out.println(list.size());
//            BsDiary  bsDiaries=db.queryOneRow("select  *  from bs_diary where d_diaryId='42'", BsDiary.class);
//            System.out.println(bsDiaries.getDHead());
//            String   head=db.querySingleTypeResult("select  d_head  from bs_diary where d_diaryId='42'", String.class);
//            System.out.println(head);
//            List<Date> list = db.querySingleTypeList("select  d_date  from bs_diary ");
//            System.out.println(list.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}