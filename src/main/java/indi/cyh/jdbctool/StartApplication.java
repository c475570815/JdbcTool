package indi.cyh.jdbctool;

import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.config.ConfigCenter;
import indi.cyh.jdbctool.config.TemplateConfig;
import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.core.JdbcDataBase;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;


public class StartApplication {


    public static void main(String[] args) {
        String sql = "SELECT *  from test_table";
        JdbcDataBase db = DataSourceFactory.getJdbcDataBase();
        System.out.println(JSONObject.toJSONString(db.queryListMap(sql)));
    }
}

