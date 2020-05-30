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

import java.util.ArrayList;
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
        try {
            JdbcDateBase db = new JdbcDateBase(null, config);
//           // 新增单个实体
//            db.insert(BsDiary.class,new BsDiary(){{
//                setDHead("head");
//            }});
//          //  关联id单个删除
//            db.delectbyId(BsDiary.class,"1");
//           // 关联id多个删除
//            db.delectbyIds(BsDiary.class, new ArrayList<Object>() {{
//                add("1");
//                add("2");
//            }});
//            //单一简单类型数据查询
//            db.querySingleTypeResult("select  d_head  from bs_diary where d_diaryId=?",String.class,"42");
//           // 多行简单类型数据查询
//            db.querySingleTypeList("select  d_head  from bs_diary ",String.class);
//           // 单一实体数据查询
//            db.queryOneRow("select  * from bs_diary  where d_diaryId=?",BsDiary.class,"42");
//           // 多行实体数据查询
//            db.queryList("select  *  from bs_diary ",BsDiary.class);
//           // Map查询
//            db.queryForMap("select  *  from bs_diary  where d_diaryId=?","42");
//          //  list查询
//            db.queryListMap("select  *  from bs_diary ");
//           // 分页查询
//            db.queryPageDate("select  *  from bs_diary",1,10,true);
//            //根据id更新
//            BsDiary diary=db.findRowById(BsDiary.class,"42");
//            diary.setDHead("update");
//            db.updateById(BsDiary.class,diary);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}