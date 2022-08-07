package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.sun.istack.internal.Nullable;
import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import indi.cyh.jdbctool.tool.LogTool;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class JdbcHandler {


    /**
     * 当前对象使用的数据池
     */
    private DruidDataSource dataSource;

    public JdbcHandler(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public DruidPooledConnection getConnecting() {
        try {
            return this.dataSource.getConnection();
        } catch (Exception e) {
            LogTool.handleLog("从连接池获取数据库连接异常:%s", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Statement getStatement() {
        try {
            return getConnecting().createStatement();
        } catch (SQLException e) {
            LogTool.handleLog("createStatement异常:%s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public DruidDataSource getDataSource() {
        return dataSource;
    }

    private boolean resultSetIsEmpty(ResultSet rs) {
        try {
            if (!rs.next()) {
                return true;
            }
            // 由于上面执行了next，所有这里需要将指针移动到上一行
            rs.previous();
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet queryResultSet(String sql, @Nullable Object... params) {
        PreparedStatement preparedStatement = setSqlParam(sql,  params);
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            LogTool.handleLog("sql预处理异常:%s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setSqlParam(String sql, @Nullable Object... params) {
        try {
            PreparedStatement preparedStatement;
            preparedStatement = getConnecting().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i, params[i]);
            }
            return preparedStatement;
        } catch (SQLException e) {
            LogTool.handleLog("sql预处理异常:%s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> rowToMap(ResultSet rs) throws SQLException {
        // 获取元数据，以便得到列的信息
        ResultSetMetaData metaData = rs.getMetaData();
        // 获取列数
        int rowColumnCount = metaData.getColumnCount();
        Map<String, Object> row = new HashMap<>();
        for (int i = 1; i <= rowColumnCount; i++) {
            row.put(metaData.getColumnName(i), rs.getObject(i));
        }
        return row;
    }

    private JSONObject rowToJsonObject(ResultSet rs) throws SQLException {
        // 获取元数据，以便得到列的信息
        ResultSetMetaData metaData = rs.getMetaData();
        // 获取列数
        int rowColumnCount = metaData.getColumnCount();
        JSONObject row = new JSONObject();
        for (int i = 1; i <= rowColumnCount; i++) {
            row.put(metaData.getColumnName(i), rs.getObject(i));
        }
        return row;
    }


    public <T> T querySingleTypeResult(String sql, Class<T> requiredType, @Nullable Object... params) throws InstantiationException, IllegalAccessException, SQLException, IOException, NoSuchFieldException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        rs.next();
        return (T) DataConvertTool.convertObject(requiredType, rs.getObject(0));

    }

    public <T> List<T> queryListSingleType(String sql, Class<T> requiredType, @Nullable Object... params) throws SQLException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return new ArrayList<>();
        }
        List<T> res = new ArrayList<>();
        while (rs.next()) {
            res.add((T) DataConvertTool.convertObject(requiredType, rs.getObject(0)));
        }
        return res;
    }

    public <T> T queryObject(String sql, Class<T> requiredType, @Nullable Object... params) throws InstantiationException, IllegalAccessException, SQLException, IOException, NoSuchFieldException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        Map<String, String> filedColumMap = EntityTool.getEntityFieldColumnMap(requiredType);
        List<String> columList = new ArrayList<>(filedColumMap.values());
        List<String> filedList = new ArrayList<>(filedColumMap.keySet());
        T row = requiredType.newInstance();
        rs.next();
        for (int i = 0; i < columList.size(); i++) {
            String columName = columList.get(i);
            String filedName = filedList.get(i);
            EntityTool.setColumValue(row, filedName, rs.getObject(columName));
        }
        return row;
    }

    public <T> List<T> queryObjectList(String sql, Class<T> requiredType, @Nullable Object... params) throws InstantiationException, IllegalAccessException, SQLException, IOException, NoSuchFieldException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        Map<String, String> filedColumMap = EntityTool.getEntityFieldColumnMap(requiredType);
        List<String> columList = new ArrayList<>(filedColumMap.values());
        List<String> filedList = new ArrayList<>(filedColumMap.keySet());
        List<T> res = new ArrayList<>();
        while (rs.next()) {
            T row = requiredType.newInstance();
            for (int i = 0; i < columList.size(); i++) {
                String columName = columList.get(i);
                String filedName = filedList.get(i);
                EntityTool.setColumValue(row, filedName, rs.getObject(columName));
            }
            res.add(row);
        }
        return res;
    }


    public Map<String, Object> queryMap(String sql, @Nullable Object... params) throws SQLException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        return rowToMap(rs);
    }

    public List<Map<String, Object>> queryListMap(String sql, @Nullable Object... params) throws SQLException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        List<Map<String, Object>> res = new ArrayList<>();
        while (rs.next()) {
            res.add(rowToMap(rs));
        }
        return res;
    }


    public JSONObject queryJsonObject(String sql, @Nullable Object... params) throws SQLException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        return rowToJsonObject(rs);
    }

    public JSONArray queryJsonArray(String sql, @Nullable Object... params) throws SQLException {
        ResultSet rs = this.queryResultSet(sql, params);
        if (resultSetIsEmpty(rs)) {
            return null;
        }
        JSONArray res = new JSONArray();
        while (rs.next()) {
            res.add(rowToJsonObject(rs));
        }
        return res;
    }


    public int update(String sql, @Nullable Object... params) {
        try {
            PreparedStatement preparedStatement = setSqlParam(sql, params);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LogTool.handleLog("更新语句执行异常:%s", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}


