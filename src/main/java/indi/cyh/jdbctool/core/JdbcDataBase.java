package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.Nullable;
import indi.cyh.jdbctool.modle.*;
import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import indi.cyh.jdbctool.tool.LogTool;
import indi.cyh.jdbctool.tool.StringTool;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @ClassName JdbcDateBase
 * @Description TODO
 * @Author gm
 * @Date 2020/4/11 8:36
 */
public class JdbcDataBase {


    /**
     * jdbc处理工具
     */
    JdbcHandler handler;
    /**
     * 日志工具
     *
     * @param null
     * @return
     * @author cyh
     * 2020/7/16 21:16
     **/
    private final LogTool log = new LogTool();
    DBType dbType;

    @Override
    public int hashCode() {
        String conn = handler.getDataSource().getUrl();
        String loginName = handler.getDataSource().getUsername();
        String pwd = handler.getDataSource().getPassword();
        return 17 * conn.hashCode() * loginName.hashCode() * pwd.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JdbcDataBase)) {
            return false;
        }
        JdbcDataBase db = (JdbcDataBase) obj;
        // 地址相等
        if (this == db) {
            return true;
        }
        return db.handler.getDataSource().getUrl().equals(this.handler.getDataSource().getUrl());
    }

    public JdbcDataBase(DruidDataSource dataSource) {
        dbType = DBType.getDbTypeByDriverClassName(dataSource.getDriverClassName());
        this.handler = new JdbcHandler(dataSource);
    }


    /**
     * 查询listmap时 转换一些特殊类型为String
     *
     * @param pageData
     * @return java.lang.Object
     * @author cyh
     * 2020/5/28 22:15
     **/
    private Object resultConvert(List<Map<String, Object>> pageData) {
        HashMap<String, ConvertType> convertMap = new HashMap<>();
        List<String> hasCheckColumnList = new ArrayList<>();
        for (Map<String, Object> row : pageData) {
            DataConvertTool.getConvertColumn(pageData.get(0), hasCheckColumnList, convertMap);
            if (convertMap.keySet().size() > 0) {
                for (String columnKey : convertMap.keySet()) {
                    Object columnData = row.get(columnKey);
                    if (columnData != null) {
                        switch (convertMap.get(columnKey)) {
                            case BYTE_TO_BASE64:
                                row.put(columnKey, DataConvertTool.byteToBase64(columnData));
                                break;
                            case TIMESTAMP_TO_STRING:
                                row.put(columnKey, DataConvertTool.timestampToString(columnData));
                                break;
                            case PGOBJECT_TO_STRING:
                                row.put(columnKey, DataConvertTool.pgObjectToString(columnData));
                            default:
                                break;
                        }
                    } else {
                        row.put(columnKey, "");
                    }
                }
            }
        }
        return pageData;
    }

    /**
     * 查询单一简单类型结果
     *
     * @param sql
     * @param requiredType
     * @param params
     * @return T
     * @author CYH
     * @date 2020/5/29 0029 16:44
     **/
    public <T> T querySingleTypeResult(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        T t = handler.querySingleTypeResult(sql, requiredType, params);
        log.handleTimeLost(start);
        return t;
    }

    /**
     * 查询简单类型集合结果
     *
     * @param sql
     * @param params
     * @return java.util.List<T>
     * @author CYH
     * @date 2020/7/10 0010 17:05
     **/
    public <T> List<T> querySingleTypeList(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        List<T> t = handler.queryListSingleType(sql, requiredType, params);
        log.handleTimeLost(start);
        return t;
    }

    /**
     * 查询一个实体类
     *
     * @param sql          sql
     * @param requiredType 返回类型
     * @param params       参数
     * @return T           目标实体
     * @author cyh
     * 2020/4/11 15:41
     **/
    public <T> T queryObject(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        T t = handler.queryObject(sql, requiredType, params);
        log.handleTimeLost(start);
        return t;
    }

    /**
     * 查询实体类集合
     *
     * @param sql
     * @param requiredType
     * @param params
     * @return java.util.List<T>
     * @author cyh
     * 2020/5/28 22:12
     **/
    public <T> List<T> queryListObject(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        List<T> t = handler.queryObjectList(sql, requiredType, params);
        log.handleTimeLost(start);
        return t;
    }

    /**
     * 查询一行 map
     *
     * @param sql
     * @param params
     * @return java.util.Map
     * @author cyh
     * 2020/5/28 22:12
     **/
    public Map<String, Object> queryMap(String sql, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        Map<String, Object> map = handler.queryMap(sql, params);
        log.handleTimeLost(start);
        return map;
    }

    /**
     * 查询获取list<Map>
     *
     * @param sql sql
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @author cyh
     * 2020/4/11 18:11
     **/
    public List<Map<String, Object>> queryListMap(String sql, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        List<Map<String, Object>> list = handler.queryListMap(sql, params);
        log.handleTimeLost(start);
        return list;
    }

    /**
     * 查询jsonObject
     *
     * @param sql
     * @param params
     * @return com.alibaba.fastjson.JSONObject
     * @author CYH
     * @date 2022/7/25 10:41
     **/
    public JSONObject queryJsonObject(String sql, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JSONObject object = handler.queryJsonObject(sql, params);
        log.handleTimeLost(start);
        return object;
    }

    /**
     * 查询JSONArray
     *
     * @param sql
     * @param params
     * @return com.alibaba.fastjson.JSONArray
     * @author CYH
     * @date 2022/7/25 10:42
     **/
    public JSONArray queryJSONArray(String sql, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JSONArray array = handler.queryJsonArray(sql, params);
        log.handleTimeLost(start);
        return array;
    }


    /**
     * 查询分页数据
     *
     * @param sql            原始sql
     * @param page           页数
     * @param rows           每页行数
     * @param isResultString 是否把结果都转换为String
     * @return Map<String, Object>
     * @author CYH
     * @date 2020/4/28 0028 16:03
     **/
    public Map<String, Object> queryPageData(String sql, Integer page, Integer rows, boolean isResultString, @Nullable Object... params) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        long start = System.currentTimeMillis();
        PageQueryInfo queryInfo = getPageQueryInfo(page, rows, sql);
        resMap.put("total", querySingleTypeResult(queryInfo.getCountSql(), int.class, params));
        List<Map<String, Object>> pageData = queryListMap(queryInfo.getPageSql(), params);
        resMap.put("pageData", isResultString ? resultConvert(pageData) : pageData);
        resMap.put("page", page);
        resMap.put("rows", rows);
        log.handleTimeLost(start);
        return resMap;
    }

    /**
     * 制作分页实体
     *
     * @param sql
     * @return java.lang.Object
     * @author CYH
     * @date 2020/4/28 0028 16:12
     **/
    private PageQueryInfo getPageQueryInfo(Integer page, Integer rows, String sql) throws Exception {
        PageQueryInfo queryInfo = new PageQueryInfo();
        queryInfo.setPageSql(SqlHandler.getPageSql(page, rows, sql, dbType));
        queryInfo.setCountSql(SqlHandler.getSelectCountSql(sql, dbType));
        return queryInfo;
    }

    /**
     * 用于执行 DML 语句(INSERT、UPDATE、DELETE)
     *
     * @param sql    sql
     * @param params 参数
     * @return int  影响行数
     * @author cyh
     * 2020/4/11 18:05
     **/
    public int executeDMLSql(String sql, @Nullable Object... params) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl(), params);
        return handler.update(sql, params);
    }

    /**
     * 插入一个实体类
     *
     * @param t
     * @return int  主键值
     * @author CYH
     * @date 2020/5/29 0029 15:44
     **/
    public <T> Object insert(Class<T> requiredType, T t, boolean returnIntPrimary) throws NoSuchFieldException, IllegalAccessException, SQLException {
        Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(requiredType);
        //语句拼接
        StringBuilder insertSqlBuilder = new StringBuilder("INSERT INTO ");
        insertSqlBuilder.append(EntityTool.getTableName(requiredType));
        insertSqlBuilder.append("(");
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        List<Object> valueList = new ArrayList<>();
        assert fieldColumnMap != null;
        for (String column : fieldColumnMap.keySet()) {
            Field field = requiredType.getDeclaredField(column);
            field.setAccessible(true);
            Object columnValue;
            if ((columnValue = field.get(t)) != null) {
                columnNameList.add(fieldColumnMap.get(column));
                valueList.add(columnValue);
                placeholderList.add("?");
            }
        }
        insertSqlBuilder.append(String.join(",", columnNameList));
        insertSqlBuilder.append(")");
        insertSqlBuilder.append(" VALUES (");
        insertSqlBuilder.append(String.join(",", placeholderList));
        insertSqlBuilder.append(")");
        String sql = insertSqlBuilder.toString();
        Object[] params = valueList.toArray();
        if (returnIntPrimary) {
            return handler.updateReturnIntPrimary(sql, params);
        } else {
            executeDMLSql(sql, params);
            return null;
        }
    }

    /**
     * 根据id 删除
     *
     * @param requiredType
     * @param id
     * @return void
     * @author CYH
     * @date 2020/5/29 0029 17:01
     **/
    public <T> void deleteById(Class<T> requiredType, Object id) {
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTableName(requiredType);
        String sql = "DELETE FROM " + tableName + "  where  " +
                primaryField + "=?";
        executeDMLSql(sql, id);
    }

    /**
     * 根据id集合  删除
     *
     * @param requiredType
     * @param ids
     * @return void
     * @author CYH
     * @date 2020/5/29 0029 17:08
     **/
    public <T> void deleteByIds(Class<T> requiredType, List<Object> ids) {
        List<String> isList = new ArrayList<>();
        for (Object id : ids) {
            isList.add(id.toString());
        }
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTableName(requiredType);
        String sql = "DELETE FROM " + tableName + "  where  " +
                primaryField + " in (" + StringTool.getSqlValueStr(isList) + ")";
        executeDMLSql(sql);
    }

    /**
     * 根据一个主键 查询一个实体类
     *
     * @param requiredType
     * @param id
     * @return T
     * @author CYH
     * @date 2020/5/29 0029 17:28
     **/
    public <T> T findRowById(Class<T> requiredType, Object id) {
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTableName(requiredType);
        String sql = "select * FROM " + tableName + "  where  " +
                primaryField + "=?";
        return queryObject(sql, requiredType, id);
    }

    /**
     * 根据主键集合 查询实体类集合
     *
     * @param requiredType
     * @param ids
     * @return java.util.List<T>
     * @author CYH
     * @date 2020/5/29 0029 17:38
     **/
    public <T> List<T> findRowsByIds(Class<T> requiredType, List<Object> ids) {
        List<String> isList = new ArrayList<>();
        for (Object id : ids) {
            isList.add(id.toString());
        }
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTableName(requiredType);
        String sql = "select * FROM " + tableName + "  where  " +
                primaryField + " in  (" + StringTool.getSqlValueStr(isList) + ")";
        return queryListObject(sql, requiredType);
    }

    /**
     * 根据主键更新
     *
     * @param requiredType
     * @param entity
     * @return q
     * @author cyh
     * 2020/5/30 11:03
     **/
    public <T> void updateById(Class<T> requiredType, T entity) throws NoSuchFieldException, IllegalAccessException {
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTableName(requiredType);
        Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(requiredType);
        Object primaryFieldValue = null;
        List<Object> valueList = new ArrayList<>();
        StringBuilder updateByIdSqlBuilder = new StringBuilder("UPDATE ");
        updateByIdSqlBuilder.append(tableName).append("  set  ");
        assert fieldColumnMap != null;
        for (String column : fieldColumnMap.keySet()) {
            Field field = requiredType.getDeclaredField(column);
            field.setAccessible(true);
            String fieldColumn = fieldColumnMap.get(column);
            if (!fieldColumn.equals(primaryField)) {
                updateByIdSqlBuilder.append(fieldColumn).append(" = ").append("?,");
                valueList.add(field.get(entity));
            } else {
                primaryFieldValue = field.get(entity);
            }
        }
        updateByIdSqlBuilder.deleteCharAt(updateByIdSqlBuilder.length() - 1);
        updateByIdSqlBuilder.append(" where ").append(primaryField).append("=").append("?");
        valueList.add(primaryFieldValue);
        String sql = updateByIdSqlBuilder.toString();
        Object[] params = valueList.toArray();
        executeDMLSql(sql, params);
    }

    /**
     * 开启一个事务
     *
     * @param
     * @return java.lang.String  返回事务id
     * @author cyh
     * 2020/7/4 9:41
     **/
    public void beginTransaction() throws Exception {
        handler.beginTransaction();
    }

    /**
     * 提交一个事务
     *
     * @return void
     * @author cyh
     * 2020/7/4 9:42
     **/
    public void commitTransaction() {
        handler.commitTransaction();
    }

    /**
     * 回滚一个事务
     *
     * @return void
     * @author cyh
     * 2020/7/4 9:42
     **/
    public void rollbackTransaction() {
        handler.rollbackTransaction();
    }

    /**
     * 用于执行DDL  sql
     *
     * @param sql
     * @return void
     * @author CYH
     * @date 2020/5/15 0015 16:34
     **/
    public void executeDDLSql(String sql) {
        log.handleSqlLog(sql, handler.getDataSource().getRawJdbcUrl());
        try {
            handler.getStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取连接池
     *
     * @param
     * @return javax.sql.DataSource
     * @author cyh
     * 2020/12/9 22:41
     **/
    public DataSource getDataSource() {
        return this.handler.getDataSource();
    }
}
