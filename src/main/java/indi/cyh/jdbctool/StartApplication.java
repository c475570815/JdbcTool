package indi.cyh.jdbctool;

import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.config.ConfigCenter;
import indi.cyh.jdbctool.config.TemplateConfig;
import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.core.JdbcDataBase;
import indi.cyh.jdbctool.entity.EstTable;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;


public class StartApplication {
    static JdbcDataBase db = DataSourceFactory.getJdbcDataBase();

    public static void main(String[] args) throws Exception {
//map查询
//        String sql = "SELECT *  from test_table";
//        System.out.println(JSONObject.toJSONString(db.queryListMap(sql)));
//实体类查询
//        String sql = "SELECT *  from test_table";
//        System.out.println(JSONObject.toJSONString(db.queryList(sql, EstTable.class)));
//插入
//        EstTable estTable = new EstTable();
//        estTable.setId(UUID.randomUUID().toString());
//        estTable.setName("t1");
//        estTable.setNote("note");
//        estTable.setNu(Long.parseLong("1231231231"));
//        estTable.setStatus("123123");
//        estTable.setCreateTime(new Date());
//        System.out.println(JSONObject.toJSONString(db.insert(EstTable.class, estTable)));
        //事务
        db.beginTransaction();
        try {
            for (int i = 0; i < 10; i++) {
                EstTable estTable = new EstTable();
                estTable.setId(UUID.randomUUID().toString());
                estTable.setName("t" + i);
                estTable.setNote("note");
                estTable.setNu(Long.parseLong("1231231231"));
                estTable.setStatus("123123");
                estTable.setCreateTime(new Date());
                db.insert(EstTable.class, estTable);
//                if (i < 8) {
//                    throw new Exception("test");
//                }
            }
            db.commitTransaction();
        } catch (Exception e) {
            db.rollbackTransaction();
        }

    }
}

