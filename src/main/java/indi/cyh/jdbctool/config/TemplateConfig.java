package indi.cyh.jdbctool.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.jdbctool.modle.DataBaseTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 类功能描述: 模板信息配置
 *
 * @author mengcaiwen
 * @version 1.0
 * @date 2021/2/23 11:27
 * @since jdk版本 1.8
 */
public class TemplateConfig {

    //jdbc连接生成相关参数
    public static final String IP = "{{IP}}";

    public static final String PORT = "{{PORT}}";

    public static final String END_PARAM = "{{END_PARAM}}";

    /**
     * 默认模板创建
     */
    private static Map<String, DataBaseTemplate> initDefaultDataBaseTemplate() {
        Map<String, DataBaseTemplate> defaultUrlTemplateMap = new HashMap<>();
        DataBaseTemplate mysql = new DataBaseTemplate() {{
            setJdbcTemplate("jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(3306);
            setDriverClassName("com.mysql.cj.jdbc.Driver");
            setType("mysql");
        }};
        DataBaseTemplate oracle = new DataBaseTemplate() {{
            setJdbcTemplate("jdbc:oracle:thin:@{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(1521);
            setDriverClassName("oracle.jdbc.driver.OracleDriver");
            setType("oracle");
        }};
        DataBaseTemplate postgres = new DataBaseTemplate() {{
            setJdbcTemplate("jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(5432);
            setDriverClassName("org.postgresql.Driver");
            setType("postgres");
        }};
        defaultUrlTemplateMap.put("mysql", mysql);
        defaultUrlTemplateMap.put("oracle", oracle);
        defaultUrlTemplateMap.put("postgres", postgres);
        return defaultUrlTemplateMap;
    }

    /**
     * JSON模板信息转化为 模板对象列表
     */
    private static List<DataBaseTemplate> getDataBaseTemplateList(JSONArray jsonArray) {
        List<DataBaseTemplate> res = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                DataBaseTemplate template = new DataBaseTemplate();
                template.setJdbcTemplate(object.getString("jdbcTemplate"));
                template.setType(object.getString("type"));
                template.setPort(object.getInteger("port"));
                template.setDriverClassName(object.getString("driverClassName"));
                res.add(template);
            }
        }
        return res;
    }

    /**
     * 添加 覆盖模板
     **/
    public static void addDataBaseTemplate(Map<String, DataBaseTemplate> dataBaseTemplateMap, DataBaseTemplate template) {
        //检查模板数据是否正确
        boolean check = template.getJdbcTemplate().contains(IP) && template.getJdbcTemplate().contains(PORT) && template.getJdbcTemplate().contains(END_PARAM);
        if (!check) {
            throw new RuntimeException("模板必须包含{{IP}}、{{PORT}}、{{END_PARAM}}");
        }
        dataBaseTemplateMap.put(template.getType(), template);
    }

    /**
     * 获取默认和配置的模板结果数据
     *
     * @param array JSON数据
     * @return 结果
     */
    public static void getDataBaseTemplate(Map<String, DataBaseTemplate> dataBaseTemplateMap, JSONArray array) {
        List<DataBaseTemplate> dataBaseTemplateList = getDataBaseTemplateList(array);
        for (DataBaseTemplate dataBaseTemplate : dataBaseTemplateList) {
            addDataBaseTemplate(dataBaseTemplateMap, dataBaseTemplate);
        }
    }
}
