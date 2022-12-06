package indi.cyh.sailjdbc.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import indi.cyh.sailjdbc.modle.DbInfo;

import java.util.ArrayList;
import java.util.List;
/**
 * 类功能描述: 数据库配置
 *
 * @author mengcaiwen
 * @version 1.0
 * @date 2021/2/23 14:47
 * @since jdk版本 1.8
 */
public class DataBaseInfoConfig {

    /**
     * 方法功能描述: 获取数据库配置相关信息
     */
    public static List<DbInfo> getDataBaseInfo(JSONArray jsonArray) {
        List<DbInfo> res = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            DbInfo dbInfo = new DbInfo();
            dbInfo.setSourceName(object.getString("sourceName"));
            dbInfo.setType(object.getString("type"));
            dbInfo.setIp(object.getString("ip"));
            dbInfo.setPort(object.getInteger("port"));
            dbInfo.setLoginName(object.getString("loginName"));
            dbInfo.setPwd(object.getString("pwd"));
            dbInfo.setEndParam(object.getString("endParam"));
            res.add(dbInfo);
        }
        return res;
    }
}
