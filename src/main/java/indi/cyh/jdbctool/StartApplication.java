package indi.cyh.jdbctool;

import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.config.ConfigCenter;
import indi.cyh.jdbctool.config.TemplateConfig;
import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.core.JdbcDataBase;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;


public class StartApplication {
    static JdbcDataBase db = DataSourceFactory.getJdbcDataBase();

    public static void main(String[] args) throws Exception {


    }
}

