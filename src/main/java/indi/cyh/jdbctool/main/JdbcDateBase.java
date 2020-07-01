package indi.cyh.jdbctool.main;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import indi.cyh.jdbctool.entity.BsDiary;
import indi.cyh.jdbctool.modle.*;
import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import indi.cyh.jdbctool.tool.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName JdbcDateBase
 * @Description TODO
 * @Author gm
 * @Date 2020/4/11 8:36
 */
public class JdbcDateBase {

    static final String IP = "{{IP}}";
    static final String PORT = "{{PORT}}";
    static final String END_PARAM = "{{END_PARAM}}";
    static final String DEFAULT_CONFIG_NAME = "application.yml";
    static final String MAIN_DB_URL_PATH = "spring.datasource.url";
    static final String MAIN_DB_URL_USERNAME_PATH = "spring.datasource.username";
    static final String MAIN_DB_URL_PWD_PATH = "spring.datasource.password";
    static final String MAIN_DB_URL_DRIVER_PATH = "spring.datasource.driver-class-name";

    private static final Pattern selectPattern;
    private static final Pattern fromPattern;
    private static final Pattern PATTERN_BRACKET;
    private static final Pattern PATTERN_SELECT;
    private static final Pattern PATTERN_DISTINCT;
    private static final Pattern rxOrderBy;


    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl;
    private static final ReentrantReadWriteLock.WriteLock wl;

    static {
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
        selectPattern = Pattern.compile("\\s*(SELECT|EXECUTE|CALL)\\s", 78);
        fromPattern = Pattern.compile("\\s*FROM\\s", 74);
        PATTERN_BRACKET = Pattern.compile("(\\(|\\)|[^\\(\\)]*)");
        PATTERN_SELECT = Pattern.compile("select([\\W\\w]*)from", 78);
        PATTERN_DISTINCT = Pattern.compile("\\A\\s+DISTINCT\\s", 78);
        rxOrderBy = Pattern.compile("\\bORDER\\s+BY\\s+([\\W\\w]*)(ASC|DESC)+", 78);
    }

    /**
     * 数据库信息实体类
     */
    public DbInfo dbInfo;

    DruidDataSource dataSource;

    /***
     * 配置文件读取的默认配置
     */
    private DbConfig defaultConfig;

    /**
     * 根据参数初始化 数据源
     *
     * @param entity 配置文件读取的默认配置
     * @param config 对象数据库信息
     * @author cyh
     * 2020/5/28 21:54
     **/
    public JdbcDateBase(DbInfo entity, DbConfig config) throws Exception {
        if (config != null) {
            this.defaultConfig = config;
            boolean isUserMainDbConfig = entity == null;
            //使用数据库默认主配或者读取次配置才去读取配置生成数据源  否则直接使用实例中给出的相应参数生成数据源
            if (isUserMainDbConfig || config.isReadConfig()) {
                //检查是否加载到配置
                if (checkDefaultConfig()) {
                    if (isUserMainDbConfig) {
                        entity = new DbInfo();
                        //加载默认主配
                        loadingMainDbConfig(entity);
                    } else if (StringTool.isNotEmpty(entity.getDbType())) {
                        //把配置结合参数转换未实体类
                        setDbInfo(entity);
                    }
                } else {
                    throw new Exception("未加载到数据库默认配置,请检查配置!");
                }
            }
        }
        //根据实体生成数据源
        loadDatebase(entity);
    }

    /**
     * 加载默认主配
     *
     * @param entity 配置文件读取的默认配置
     * @return void
     * @author cyh
     * 2020/5/28 21:55
     **/
    private void loadingMainDbConfig(DbInfo entity) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        String configFileName = StringTool.isEmpty(defaultConfig.getConfigFileName()) ? DEFAULT_CONFIG_NAME : defaultConfig.getConfigFileName();
        yaml.setResources(new ClassPathResource(configFileName));
        Properties properties = yaml.getObject();
        entity.setConnectStr(String.valueOf(properties.get(MAIN_DB_URL_PATH)));
        entity.setLogoinName(String.valueOf(properties.get(MAIN_DB_URL_USERNAME_PATH)));
        entity.setPwd(String.valueOf(properties.get(MAIN_DB_URL_PWD_PATH)));
        entity.setDriverClassName(String.valueOf(properties.get(MAIN_DB_URL_DRIVER_PATH)));
    }

    /**
     * 生成一个数据源
     *
     * @param dbInfo 数据源信息
     * @return void
     * @author cyh
     * 2020/5/28 21:58
     **/
    private void loadDatebase(DbInfo dbInfo) throws SQLException {
        wl.lock();
        rl.lock();
        dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbInfo.getDriverClassName());
        dataSource.setUrl(dbInfo.getConnectStr());
        dataSource.setUsername(dbInfo.getLogoinName());
        dataSource.setPassword(dbInfo.getPwd());
        rl.unlock();
        wl.unlock();
    }

    /**
     * 获取是否加载到配置
     *
     * @param
     * @return boolean
     * @author cyh
     * 2020/5/28 21:59
     **/
    private boolean checkDefaultConfig() {
        return defaultConfig != null;
    }

    /**
     * 加载给出的数据源信息
     *
     * @param entity 配置文件读取的默认配置
     * @return void
     * @author cyh
     * 2020/5/28 21:57
     **/
    private void setDbInfo(DbInfo entity) throws Exception {
        List<DbInfo> defalutConfigList = defaultConfig.getDefalutConfigList();
        for (DbInfo config : defalutConfigList) {
            if (entity.getDbType().equals(config.getDbType())) {
                entity.setConnectStr(getDbConnectUrl(config, entity));
                entity.setDriverClassName(config.getDriverClassName());
                entity.setUrlTemplate(config.getUrlTemplate());
                if (StringTool.isEmpty(entity.getIp())) {
                    entity.setIp(config.getIp());
                }
                return;
            }
        }
        throw new Exception("不支持的数据源类型!");
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
     * 根据db信息生成对应的jdbc链接
     *
     * @param config
     * @param entity
     * @return java.lang.String
     * @author cyh
     * 2020/5/28 21:59
     **/
    private String getDbConnectUrl(DbInfo config, DbInfo entity) throws Exception {
        String urlTemplate = config.getUrlTemplate();
        urlTemplate = urlTemplate.replace(IP, entity.getIp());
        urlTemplate = urlTemplate.replace(PORT, String.valueOf(entity.getPort()));
        urlTemplate = urlTemplate.replace(END_PARAM, entity.getEndParam());
        return urlTemplate;
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
        JdbcTemplate template = getJdbcTemplate();
        return (T) template.queryForObject(sql, requiredType, params);
    }

    /**
     * 查询简单类型集合结果
     *
     * @param sql
     * @param params
     * @return java.util.List<T>
     * @author cyh
     * 2020/5/28 22:21
     **/
    public <T> List<T> querySingleTypeList(String sql, @Nullable Object... params) {
        return getJdbcTemplate().query(sql, new RowMapper<T>() {
            @Override
            public T mapRow(ResultSet resultSet, int i) throws SQLException {
                return (T) resultSet.getObject(1);
            }
        });
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
        JdbcTemplate template = getJdbcTemplate();
        return (T) template.queryForObject(sql, new JdbcRowMapper<T>(requiredType), params);
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
        Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(requiredType);
        JdbcTemplate template = getJdbcTemplate();
        return template.query(sql, new JdbcRowMapper<T>(requiredType), params);
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
        JdbcTemplate template = getJdbcTemplate();
        return template.queryForMap(sql, params);
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
        JdbcTemplate template = getJdbcTemplate();
        return template.queryForList(sql, params);
    }

    /**
     * 查询分页数据
     *
     * @param serviceSql     原始sql
     * @param page           页数
     * @param rows           每页行数
     * @param isResultString 是否把结果都转换为String
     * @return Map<String, Object>
     * @author CYH
     * @date 2020/4/28 0028 16:03
     **/
    public Map<String, Object> queryPageDate(String serviceSql, Integer page, Integer rows, boolean isResultString, @Nullable Object... params) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        PageQueryInfo queryInfo = getPageQueryInfo(page, rows, serviceSql);
        resMap.put("total", querySingleTypeResult(queryInfo.getCountSql(), int.class, params));
        List<Map<String, Object>> pageDate = queryListMap(queryInfo.getPageSql(), params);
        resMap.put("pageDate", isResultString ? resultConvert(pageDate) : pageDate);
        resMap.put("page", page);
        resMap.put("rows", rows);
        return resMap;
    }

    /**
     * 获取数据总条数
     *
     * @param serviceSql
     * @return java.lang.Object
     * @author CYH
     * @date 2020/4/28 0028 16:12
     **/
    private PageQueryInfo getPageQueryInfo(Integer page, Integer rows, String serviceSql) throws Exception {
        long skip = (long) ((page - 1) * rows);
        Matcher matcherSelect = PATTERN_SELECT.matcher(serviceSql);
        if (!matcherSelect.find()) {
            throw new Exception("build paging querySql error:canot find select from");
        } else {
            String sqlSelectCols = matcherSelect.group(1);
            String countSql = String.format("select COUNT(1) from (%s) pageTable", serviceSql);
            Matcher matcherDistinct = PATTERN_DISTINCT.matcher(sqlSelectCols);
            int lastOrderIndex = serviceSql.toLowerCase().lastIndexOf("order");
            String sqlOrderBy = null;
            if (lastOrderIndex > -1) {
                sqlOrderBy = serviceSql.substring(lastOrderIndex);
            }

            int firstSelectIndex = serviceSql.toLowerCase().indexOf("select");
            String formatSQL = "";
            if (!matcherDistinct.find() && !"*".equals(sqlSelectCols.trim().toLowerCase())) {
                formatSQL = serviceSql.substring(firstSelectIndex + 6);
            } else {
                formatSQL = " peta_table.* from (" + serviceSql + ") peta_table ";
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
     * @param pageDate
     * @return java.lang.Object
     * @author cyh
     * 2020/5/28 22:15
     **/
    private Object resultConvert(List<Map<String, Object>> pageDate) {
        HashMap<String, ConvertType> convertMap = new HashMap<>();
        List<String> hasCheckColumnList = new ArrayList<>();
        for (Map<String, Object> row : pageDate) {
            DataConvertTool.getConvertColumn(pageDate.get(0), hasCheckColumnList, convertMap);
            if (convertMap.keySet().size() > 0) {
                for (String columnKey : convertMap.keySet()) {
                    Object columnDate = row.get(columnKey);
                    if (columnDate != null) {
                        switch (convertMap.get(columnKey)) {
                            case BYTE_TO_BASE64:
                                row.put(columnKey, DataConvertTool.byteToBase64(columnDate));
                                break;
                            case TIMESTAMP_TO_STRING:
                                row.put(columnKey, DataConvertTool.timestampToString(columnDate));
                                break;
                            case PGOBJECT_TO_STRING:
                                row.put(columnKey, DataConvertTool.pgObjectToString(columnDate));
                            default:
                                break;
                        }
                    } else {
                        row.put(columnKey, "");
                    }
                }
            }
        }
        return pageDate;
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
        //System.out.println(sql);
        executeDMLSql(sql, valueList.toArray());
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
        StringBuilder dfindRowByIdSqlBuilder = new StringBuilder("select * FROM ");
        dfindRowByIdSqlBuilder.append(tableName + "  where  ");
        dfindRowByIdSqlBuilder.append(primaryField).append(" in  (" + StringTool.getSqlValueStr(isList) + ")");
        String sql = dfindRowByIdSqlBuilder.toString();
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
        System.out.println(sql);
        executeDMLSql(sql, valueList.toArray());
    }
}
