package indi.cyh.jdbctool;

import indi.cyh.jdbctool.core.DataSourceFactory;
import indi.cyh.jdbctool.core.JdbcDataBase;


public class StartApplication {
    public static void main(String[] args) {
        try {
            JdbcDataBase db = DataSourceFactory.getJdbcDataBase();
            String sql = "select d_head from bs_diary\n";
            System.out.println(String.join(",", db.querySingleTypeList(sql, String.class)));
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

