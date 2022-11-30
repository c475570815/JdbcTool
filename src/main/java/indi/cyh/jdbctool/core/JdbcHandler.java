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

    private DruidPooledConnection sharedConnection;

    public JdbcHandler(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }


    private boolean IsTransaction = false;


    public DruidPooledConnection getConnecting() {
        try {
            if (this.sharedConnection == null || this.sharedConnection.isClosed()) {
                this.sharedConnection = this.dataSource.getConnection();
            }
            return this.sharedConnection;
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
        PreparedStatement preparedStatement = setSqlParam(sql, false, params);
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            LogTool.handleExceptionLog("sql预处理异常", true, e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setSqlParam(String sql, boolean isUpdate, @Nullable Object... params) {
        try {
            PreparedStatement preparedStatement;
            preparedStatement = getConnecting().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, isUpdate ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement;
        } catch (SQLException e) {
            LogTool.handleExceptionLog("sql预处理异常", true, e);
            throw new RuntimeException(e);
        }
    }

    private void closePreparedStatement(PreparedStatement preparedStatement) {
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            LogTool.handleExceptionLog("关闭预处理对象异常", true, e);
            throw new RuntimeException(e);
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            LogTool.handleExceptionLog("ResultSet关闭异常", false, e);
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() {
        try {
            if (!this.IsTransaction && this.sharedConnection != null && !this.sharedConnection.isClosed()) {
                this.sharedConnection.close();
            }
        } catch (SQLException e) {
            LogTool.handleLog(("关闭数据库连接异常!" + e.getMessage() + ((DruidDataSource) this.dataSource).getUrl()));
            throw new RuntimeException("close connection error", e);
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


    public <T> T querySingleTypeResult(String sql, Class<T> requiredType, @Nullable Object... params) {
        ResultSet rs = null;
        rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return null;
            }
            rs.next();
            return (T) DataConvertTool.convertObject(requiredType, rs.getObject(1));
        } catch (SQLException e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }


    public <T> List<T> queryListSingleType(String sql, Class<T> requiredType, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return new ArrayList<>();
            }
            List<T> res = new ArrayList<>();
            while (rs.next()) {
                res.add((T) DataConvertTool.convertObject(requiredType, rs.getObject(1)));
            }
            return res;
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }

    public <T> T queryObject(String sql, Class<T> requiredType, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return null;
            }
            rs.next();
            return rowToObject(requiredType, rs);
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }

    private <T> T rowToObject(Class<T> requiredType, ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException, IOException, NoSuchFieldException {
        Map<String, String> filedColumMap = EntityTool.getEntityFieldColumnMap(requiredType);
        List<String> columList = new ArrayList<>(filedColumMap.values());
        List<String> filedList = new ArrayList<>(filedColumMap.keySet());
        T row = requiredType.newInstance();
        for (int i = 0; i < columList.size(); i++) {
            String columName = columList.get(i);
            String filedName = filedList.get(i);
            EntityTool.setColumValue(row, filedName, rs.getObject(columName));
        }
        return row;
    }

    public <T> List<T> queryObjectList(String sql, Class<T> requiredType, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
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
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }


    public Map<String, Object> queryMap(String sql, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return null;
            }
            return rowToMap(rs);
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }

    public List<Map<String, Object>> queryListMap(String sql, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return null;
            }
            List<Map<String, Object>> res = new ArrayList<>();
            while (rs.next()) {
                res.add(rowToMap(rs));
            }
            return res;
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }


    public JSONObject queryJsonObject(String sql, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return null;
            }
            rs.next();
            return rowToJsonObject(rs);
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }

    public JSONArray queryJsonArray(String sql, @Nullable Object... params) {
        ResultSet rs = this.queryResultSet(sql, params);
        try {
            if (resultSetIsEmpty(rs)) {
                return null;
            }
            JSONArray res = new JSONArray();
            while (rs.next()) {
                res.add(rowToJsonObject(rs));
            }
            return res;
        } catch (Exception e) {
            LogTool.handleExceptionLog("查询异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closeConnection();
        }
    }


    public int update(String sql, @Nullable Object... params) {
        PreparedStatement preparedStatement = setSqlParam(sql, false, params);
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LogTool.handleExceptionLog("更新语句执行异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection();
        }
    }

    public Object updateReturnIntPrimary(String sql, Object[] params) {
        //返回主键预处理
        PreparedStatement preparedStatement = setSqlParam(sql, false, PreparedStatement.RETURN_GENERATED_KEYS, params);
        try {
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            LogTool.handleLog("获取主键失败");
            return "";
        } catch (SQLException e) {
            LogTool.handleExceptionLog("更新语句执行异常", false, e);
            throw new RuntimeException(e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection();
        }
    }

    public void beginTransaction() {
        try {
            this.IsTransaction = true;
            this.getConnecting();
            this.sharedConnection.setAutoCommit(false);
        } catch (SQLException var2) {
            LogTool.handleLog("启动事务异常!" + var2.getMessage());
        }
    }

    public void commitTransaction() {
        try {
            if (this.sharedConnection != null) {
                this.sharedConnection.commit();
                this.sharedConnection.setAutoCommit(true);
            }
            this.IsTransaction = false;
            this.closeConnection();
        } catch (SQLException var2) {
            LogTool.handleLog("提交事务异常!" + var2.getMessage());
        }
    }


    public void rollbackTransaction() {
        try {
            if (this.sharedConnection != null) {
                this.sharedConnection.rollback();
                this.sharedConnection.setAutoCommit(true);
            }
            this.IsTransaction = false;
            this.closeConnection();
        } catch (SQLException var2) {
            LogTool.handleLog("回滚事务异常!" + var2.getMessage());
        }
    }
}


