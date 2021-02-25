package indi.cyh.jdbctool;

import indi.cyh.jdbctool.config.ConfigCenter;
import indi.cyh.jdbctool.config.TemplateConfig;
import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.core.JdbcDataBase;
import indi.cyh.jdbctool.modle.DataBaseTemplate;
import indi.cyh.jdbctool.modle.DbInfo;


public class StartApplication {


    public static void main(String[] args) {
        String sql = "select d_head from bs_diary\n";

        try {
//            //从配置获取连接信息方式
//            JdbcDataBase db = DataSourceFactory.getJdbcDataBase();
//            System.out.println(String.join(",", db.querySingleTypeList(sql, String.class)));



//            //依赖模板方式
//            DataBaseTemplate mysql = new DataBaseTemplate() {{
//                setJdbcTemplate("jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}");
//                setPort(3306);
//                setDriverClassName("com.mysql.cj.jdbc.Driver");
//                setType("mysql-test");
//            }};
//            DbInfo info = new DbInfo() {{
//                setSourceName("testDb");
//                setType("mysql-test");
//                setIp("106.52.167.158");
//                setPort(3306);
//                setLoginName("singlewood");
//                setPwd("singlewood");
//                setEndParam("singlewood?serverTimezone=UTC");
//            }};
//            ConfigCenter.addDataBaseTemplate(mysql);
//            JdbcDataBase db = DataSourceFactory.getJdbcDataBaseByInfo(info, true);
//            System.out.println(String.join(",", db.querySingleTypeList(sql, String.class)));


            //不依赖任何模式方式
            DbInfo  info = new DbInfo() {{
                setConnectStr("jdbc:mysql://106.52.167.158:3306/singlewood?serverTimezone=UTC");
                setDriverClassName("com.mysql.cj.jdbc.Driver");
                setLoginName("singlewood");
                setPwd("singlewood");
            }};
            JdbcDataBase db = DataSourceFactory.getJdbcDataBaseByInfo(info, false);
            System.out.println(String.join(",", db.querySingleTypeList(sql, String.class)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

