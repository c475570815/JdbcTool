package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.entity.BsDiary;
import indi.cyh.jdbctool.modle.*;
import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import indi.cyh.jdbctool.tool.LogTool;
import indi.cyh.jdbctool.tool.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName JdbcDateBase
 * @Description TODO
 * @Author gm
 * @Date 2020/4/11 8:36
 */
public class JdbcDataBase {


    //sql 匹配相应参数
    private static final Pattern selectPattern;
    private static final Pattern fromPattern;
    private static final Pattern PATTERN_BRACKET;
    private static final Pattern PATTERN_SELECT;
    private static final Pattern PATTERN_DISTINCT;
    private static final Pattern rxOrderBy;

    ////事务相关
    private static final TransactionDefinition definition;
    private final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    private final LinkedHashMap<String, TransactionStatus> transcationMap = new LinkedHashMap<>();

    static {
        //sql 匹配相应参数 赋值
        selectPattern = Pattern.compile("\\s*(SELECT|EXECUTE|CALL)\\s", 78);
        fromPattern = Pattern.compile("\\s*FROM\\s", 74);
        PATTERN_BRACKET = Pattern.compile("(\\(|\\)|[^\\(\\)]*)");
        PATTERN_SELECT = Pattern.compile("select([\\W\\w]*)from", 78);
        PATTERN_DISTINCT = Pattern.compile("\\A\\s+DISTINCT\\s", 78);
        rxOrderBy = Pattern.compile("\\bORDER\\s+BY\\s+([\\W\\w]*)(ASC|DESC)+", 78);
        //事务参数赋默认值
        definition = new TransactionDefinition() {
            @Override
            public int getPropagationBehavior() {
                return TransactionDefinition.PROPAGATION_REQUIRED;
            }

            @Override
            public int getIsolationLevel() {
                return TransactionDefinition.ISOLATION_DEFAULT;
            }

            @Override
            public int getTimeout() {
                return TIMEOUT_DEFAULT;
            }

            @Override
            public boolean isReadOnly() {
                return false;
            }

            @Override
            public String getName() {
                return "JDBC-TOOL";
            }
        };

    }


    /**
     * 数据库信息实体类
     */
    public DbInfo dbInfo;
    /**
     * 当前对象使用的数据池
     */
    DruidDataSource dataSource = null;

    /**
     * 从配置文件中获取是否为调试模式
     */
    private final boolean isDebugger = DataSourceFactory.getIsDebugger();
    /**
     * 日志工具
     *
     * @param null
     * @return
     * @author cyh
     * 2020/7/16 21:16
     **/
    private final LogTool log = new LogTool(isDebugger);

    public JdbcDataBase(DruidDataSource dataSource) {
        this.dataSource = dataSource;
        transactionManager.setDataSource(dataSource);
    }

    /**
     * 根据当前对象数据源 生成 JdbcTemplate
     *
     * @return org.springframework.jdbc.core.JdbcTemplate
     * @author cyh
     * 2020/5/28 21:59
     **/
    public JdbcTemplate getJdbcTemplate() {
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(this.dataSource);
        return template;
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
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JdbcTemplate template = getJdbcTemplate();
        T t = (T) template.queryForObject(sql, requiredType, params);
        log.printTimeLost(start);
        return t;
    }

    /**
     * 查询简单类型集合结果
     *
     * @param sql
     * @param requiredType
     * @param params
     * @return java.util.List<T>
     * @author CYH
     * @date 2020/7/10 0010 17:05
     **/
    public <T> List<T> querySingleTypeList(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        List<T> t = getJdbcTemplate().query(sql, new RowMapper<T>() {
            @Override
            public T mapRow(ResultSet resultSet, int i) throws SQLException {
                return (T) resultSet.getObject(1);
            }
        });
        log.printTimeLost(start);
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
    public <T> T queryOneRow(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JdbcTemplate template = getJdbcTemplate();
        T t = (T) template.queryForObject(sql, new JdbcRowMapper<T>(requiredType), params);
        log.printTimeLost(start);
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
    public <T> List<T> queryList(String sql, Class<T> requiredType, @Nullable Object... params) {
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JdbcTemplate template = getJdbcTemplate();
        List<T> t = template.query(sql, new JdbcRowMapper<T>(requiredType), params);
        log.printTimeLost(start);
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
    public Map queryForMap(String sql, @Nullable Object... params) {
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JdbcTemplate template = getJdbcTemplate();
        Map map = template.queryForMap(sql, params);
        log.printTimeLost(start);
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
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        long start = System.currentTimeMillis();
        JdbcTemplate template = getJdbcTemplate();
        List<Map<String, Object>> list = template.queryForList(sql, params);
        log.printTimeLost(start);
        return list;
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
        log.printTimeLost(start);
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
        long skip = (long) ((page - 1) * rows);
        Matcher matcherSelect = PATTERN_SELECT.matcher(sql);
        if (!matcherSelect.find()) {
            throw new Exception("build paging querySql error:canot find select from");
        } else {
            String sqlSelectCols = matcherSelect.group(1);
            String countSql = String.format("select COUNT(1) from (%s) pageTable", sql);
            Matcher matcherDistinct = PATTERN_DISTINCT.matcher(sqlSelectCols);
            int lastOrderIndex = sql.toLowerCase().lastIndexOf("order");
            String sqlOrderBy = null;
            if (lastOrderIndex > -1) {
                sqlOrderBy = sql.substring(lastOrderIndex);
            }

            int firstSelectIndex = sql.toLowerCase().indexOf("select");
            String formatSQL = "";
            if (!matcherDistinct.find() && !"*".equals(sqlSelectCols.trim().toLowerCase())) {
                formatSQL = sql.substring(firstSelectIndex + 6);
            } else {
                formatSQL = " peta_table.* from (" + sql + ") peta_table ";
                sqlOrderBy = sqlOrderBy == null ? null : sqlOrderBy.replaceAll("([A-Za-z0-9_]*)\\.", "peta_table.");
            }

            String pageSql = String.format("SELECT * FROM (SELECT ROW_NUMBER() OVER (%s) peta_rn, %s) peta_paged WHERE peta_rn>" + skip + " AND peta_rn<=" + (skip + (long) rows) + "", sqlOrderBy == null ? "ORDER BY NULL" : sqlOrderBy, formatSQL);
            PageQueryInfo queryInfo = new PageQueryInfo();
            queryInfo.setPageSql(pageSql);
            queryInfo.setCountSql(countSql);
            return queryInfo;
        }
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
     * 用于执行 DML 语句(INSERT、UPDATE、DELETE)
     *
     * @param sql    sql
     * @param params 参数
     * @return int  影响行数
     * @author cyh
     * 2020/4/11 18:05
     **/
    public int executeDMLSql(String sql, @Nullable Object... params) {
        log.printLog(sql, dataSource.getRawJdbcUrl(), params);
        JdbcTemplate template = getJdbcTemplate();
        return template.update(sql, params);
    }

    /**
     * 插入一个实体类
     *
     * @param t
     * @return int  主键值
     * @author CYH
     * @date 2020/5/29 0029 15:44
     **/
    public <T> void insert(Class<T> requiredType, T t) throws NoSuchFieldException, IllegalAccessException {
        Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(requiredType);
        StringBuilder insertSqlBuilder = new StringBuilder("INSERT INTO ");
        insertSqlBuilder.append(EntityTool.getTabelName(requiredType));
        insertSqlBuilder.append("(");
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        List<Object> valueList = new ArrayList<>();
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
        insertSqlBuilder.append(StringUtils.join(columnNameList, ","));
        insertSqlBuilder.append(")");
        insertSqlBuilder.append(" VALUES (");
        insertSqlBuilder.append(StringUtils.join(placeholderList, ","));
        insertSqlBuilder.append(")");
        String sql = insertSqlBuilder.toString();
        Object[] params = valueList.toArray();
        executeDMLSql(sql, params);
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
    public <T> void delectbyId(Class<T> requiredType, Object id) {
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTabelName(requiredType);
        StringBuilder delectSqlBuilder = new StringBuilder("DELETE FROM ");
        delectSqlBuilder.append(tableName + "  where  ");
        delectSqlBuilder.append(primaryField).append("=?");
        String sql = delectSqlBuilder.toString();
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
    public <T> void delectbyIds(Class<T> requiredType, List<Object> ids) {
        List<String> isList = new ArrayList<>();
        for (Object id : ids) {
            isList.add(id.toString());
        }
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTabelName(requiredType);
        StringBuilder delectSqlBuilder = new StringBuilder("DELETE FROM ");
        delectSqlBuilder.append(tableName + "  where  ");
        delectSqlBuilder.append(primaryField).append(" in (" + StringTool.getSqlValueStr(isList) + ")");
        String sql = delectSqlBuilder.toString();
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
        String tableName = EntityTool.getTabelName(requiredType);
        StringBuilder dfindRowByIdSqlBuilder = new StringBuilder("select * FROM ");
        dfindRowByIdSqlBuilder.append(tableName + "  where  ");
        dfindRowByIdSqlBuilder.append(primaryField).append("=?");
        String sql = dfindRowByIdSqlBuilder.toString();
        return queryOneRow(sql, requiredType, id);
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
        String tableName = EntityTool.getTabelName(requiredType);
        StringBuilder findRowByIdSqlBuilder = new StringBuilder("select * FROM ");
        findRowByIdSqlBuilder.append(tableName + "  where  ");
        findRowByIdSqlBuilder.append(primaryField).append(" in  (" + StringTool.getSqlValueStr(isList) + ")");
        String sql = findRowByIdSqlBuilder.toString();
        return queryList(sql, requiredType);
    }

    /**
     * 根据主键更新
     *
     * @param requiredType
     * @param diary
     * @return q
     * @author cyh
     * 2020/5/30 11:03
     **/
    public void updateById(Class<BsDiary> requiredType, BsDiary diary) throws NoSuchFieldException, IllegalAccessException {
        String primaryField = EntityTool.getEntityPrimaryField(requiredType);
        String tableName = EntityTool.getTabelName(requiredType);
        Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(requiredType);
        Object primaryFieldValue = null;
        List<Object> valueList = new ArrayList<>();
        StringBuilder updateByIdSqlBuilder = new StringBuilder("UPDATE ");
        updateByIdSqlBuilder.append(tableName + "  set  ");
        for (String column : fieldColumnMap.keySet()) {
            Field field = requiredType.getDeclaredField(column);
            field.setAccessible(true);
            String fieldColumn = fieldColumnMap.get(column);
            if (!fieldColumn.equals(primaryField)) {
                updateByIdSqlBuilder.append(fieldColumn).append(" = ").append("?,");
                valueList.add(field.get(diary));
            } else {
                primaryFieldValue = field.get(diary);
            }
        }
        updateByIdSqlBuilder.deleteCharAt(updateByIdSqlBuilder.length() - 1);
        updateByIdSqlBuilder.append(" where ").append(primaryField + "=").append("?");
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
    public String beginTransaction() throws Exception {
        try {
            TransactionStatus transactionStatus = transactionManager.getTransaction(definition);
            String transactionId = UUID.randomUUID().toString();
            transcationMap.put(transactionId, transactionStatus);
            System.out.println("开启事务: " + transactionId);
            return transactionId;
        } catch (Exception e) {
            throw new Exception("事务开启异常:" + e.getMessage());
        }
    }

    /**
     * 提交一个事务
     *
     * @param transactionId 事务id
     * @return void
     * @author cyh
     * 2020/7/4 9:42
     **/
    public void commitTransaction(String transactionId) {
        if (transcationMap.containsKey(transactionId)) {
            try {
                transactionManager.commit(transcationMap.get(transactionId));
                transcationMap.remove(transactionId);
                System.out.println("事务已提交: " + transactionId);
            } catch (Exception e) {
                System.out.println("事务提交异常" + transactionId + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("事务提交异常--错误的transactionId:" + transactionId);
        }

    }

    /**
     * 回滚一个事务
     *
     * @param transactionId 事务id
     * @return void
     * @author cyh
     * 2020/7/4 9:42
     **/
    public void rollbackTransaction(String transactionId) {
        if (transcationMap.containsKey(transactionId)) {
            try {
                transactionManager.rollback(transcationMap.get(transactionId));
                transcationMap.remove(transactionId);
            } catch (Exception e) {
                System.out.println("事务回归异常" + transactionId + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("事务回归异常--错误的transactionId:" + transactionId);
        }
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
        log.printLog(sql, dataSource.getRawJdbcUrl());
        JdbcTemplate template = getJdbcTemplate();
        template.execute(sql);
    }
}