package indi.cyh.jdbctool.main;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.config.DbConfig;
import indi.cyh.jdbctool.entity.BsDiary;
import indi.cyh.jdbctool.modle.*;
import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import indi.cyh.jdbctool.tool.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName JdbcDateBase
 * @Description TODO
 * @Author gm
 * @Date 2020/4/11 8:36
 */
public class JdbcDataBase {

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

    private static final TransactionDefinition definition;

    private static Map<DbInfo, DruidDataSource> listDbSource = new LinkedHashMap<>();

    /**
     * 事务相关
     */
    private DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    private LinkedHashMap<String, TransactionStatus> transcationMap = new LinkedHashMap<>();
    /**
     * 默认主配数据库
     */
    public static DruidDataSource mianDataSource;

    static {
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
        selectPattern = Pattern.compile("\\s*(SELECT|EXECUTE|CALL)\\s", 78);
        fromPattern = Pattern.compile("\\s*FROM\\s", 74);
        PATTERN_BRACKET = Pattern.compile("(\\(|\\)|[^\\(\\)]*)");
        PATTERN_SELECT = Pattern.compile("select([\\W\\w]*)from", 78);
        PATTERN_DISTINCT = Pattern.compile("\\A\\s+DISTINCT\\s", 78);
        rxOrderBy = Pattern.compile("\\bORDER\\s+BY\\s+([\\W\\w]*)(ASC|DESC)+", 78);

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


        //加载默认主配
        try {
            DbInfo entity = new DbInfo();
            loadingMainDbConfig(entity, "");
            mianDataSource = getNewDataSource(entity);
        } catch (Exception e) {
            System.out.printf("主数据库加载失败! ---" + e.getMessage());
        }

    }

    /**
     * 数据库信息实体类
     */
    public DbInfo dbInfo;

    DruidDataSource dataSource = null;


    /***
     * 配置文件读取的默认配置
     */
    private DbConfig defaultConfig;
    /**
     * defaultConfig  是否非空
     */
    private boolean hasConfig;

    private boolean useMianDatabaseSource = true;

    private void printLog(String sql, @Nullable Object... params) {
        //默认打开打印  当配置中设置了非调试模式则关闭打印
        if (!hasConfig || defaultConfig.isDebugger()) {
            System.out.println("##########################################JDBCTOOL##########################################");
            System.out.println("conStr:" + dataSource.getRawJdbcUrl());
            System.out.println("");
            System.out.println("sql : " + sql);
            if (params != null && params.length != 0) {
                System.out.println("");
                System.out.println(params.length + "  params");
                System.out.println("");
                for (int i = 0; i < params.length; i++) {
                    System.out.println("param-" + (i + 1) + ": " + String.valueOf(params[i]) + "[" + params[i].getClass().getName() + "]");
                    System.out.println("");
                }
            }
            System.out.println("##########################################JDBCTOOL##########################################");
        }
    }

    /**
     * 根据参数初始化 数据源
     *
     * @param entity 配置文件读取的默认配置
     * @param config 对象数据库信息
     * @author cyh
     * 2020/5/28 21:54
     **/
    public JdbcDataBase(DbInfo entity, DbConfig config) throws Exception {
        hasConfig = config != null;
        if (hasConfig) {
            this.defaultConfig = config;
            boolean isUserMainDbConfig = entity == null;
            //使用数据库默认配置  或者 使用实例中给出的相应参数生成数据源
            if (isUserMainDbConfig) {
                entity = new DbInfo();
                //加载默认主配
                loadingMainDbConfig(entity, defaultConfig.getConfigFileName());
            } else if (StringUtils.isNotEmpty(entity.getDbType()) && StringUtils.isEmpty(entity.getConnectStr())) {
                //把配置结合参数转换未实体类
                setDbInfo(entity);
            }
        }
        //根据实体生成数据源
        loadDatabase(entity);
        useMianDatabaseSource = false;
    }

    private JdbcDataBase() {

    }

    public static JdbcDataBase getMainJdbcDataBase() throws Exception {
        JdbcDataBase db = new JdbcDataBase();
        db.dataSource = mianDataSource;
        db.transactionManager.setDataSource(db.dataSource);
        return db;
    }

    /**
     * 加载默认主配
     *
     * @param entity 配置文件读取的默认配置
     * @return void
     * @author cyh
     * 2020/5/28 21:55
     **/
    private static void loadingMainDbConfig(DbInfo entity, String configFileName) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        String fileName = StringUtils.isEmpty(configFileName) ? DEFAULT_CONFIG_NAME : configFileName;
        yaml.setResources(new ClassPathResource(fileName));
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
    private void loadDatabase(DbInfo dbInfo) {
        DruidDataSource dataSource = getExitDataSource(dbInfo);
        if (dataSource == null) {
            dataSource = getNewDataSource(dbInfo);
            transactionManager.setDataSource(dataSource);
        }
    }

    /**
     * 生成新的连接池
     *
     * @param dbInfo
     * @return void
     * @author CYH
     * @date 2020/7/10 0010 15:49
     **/
    private static DruidDataSource getNewDataSource(DbInfo dbInfo) {
        System.out.println("新增连接池-连接:  " + dbInfo.getConnectStr());
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbInfo.getDriverClassName());
        dataSource.setUrl(dbInfo.getConnectStr());
        dataSource.setUsername(dbInfo.getLogoinName());
        dataSource.setPassword(dbInfo.getPwd());
        //配置初始化大小、最小、最大
        dataSource.setInitialSize(10);
        dataSource.setMinIdle(10);
        //最大活跃数
        dataSource.setMaxActive(20);
        //配置从连接池获取连接等待超时的时间
        dataSource.setMaxWait(10000);
        //配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
        //检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
        // 2.在minIdle以内的不处理。
        dataSource.setTimeBetweenEvictionRunsMillis(600000);
        // 配置一个连接在池中最大空闲时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);
        //  验证连接语句
        // dataSource.setValidationQuery(false);
        //设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查
        dataSource.setTestWhileIdle(false);
        //设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        dataSource.setTestOnBorrow(false);
        // 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        dataSource.setTestOnReturn(false);
        //打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料
        dataSource.setPoolPreparedStatements(true);
        //
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        //根据自身业务及事务大小来设置
        dataSource.setConnectionProperties("oracle.net.CONNECT_TIMEOUT=6000;oracle.jdbc.ReadTimeout=180000");
        //打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性. 参考：https://github.com/alibaba/druid/wiki/KeepAlive_cn
        dataSource.setKeepAlive(true);
        // 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。
        dataSource.setRemoveAbandoned(true);

        dataSource.setRemoveAbandonedTimeout(80);
        wl.lock();
        listDbSource.put(dbInfo, dataSource);
        wl.unlock();
        System.out.println("目前连接池数量: " + listDbSource.size());
        return dataSource;
    }

    /**
     * 获取已存在的连接池
     *
     * @param dbInfo
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author CYH
     * @date 2020/7/10 0010 16:00
     **/
    private DruidDataSource getExitDataSource(DbInfo dbInfo) {
        rl.lock();
        for (DbInfo info : listDbSource.keySet()) {
            if (DbInfo.equals(info, dbInfo)) {
                System.out.println("获取到已有连接池:" + info.getConnectStr());
                return listDbSource.get(info);
            }
        }
        rl.unlock();
        return null;

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
        if (defalutConfigList == null) {
            throw new Exception("未读取到连接串模板配置!");
        }
        for (DbInfo config : defalutConfigList) {
            if (entity.getDbType().equals(config.getDbType())) {
                entity.setConnectStr(getDbConnectUrl(config, entity));
                entity.setDriverClassName(config.getDriverClassName());
                entity.setUrlTemplate(config.getUrlTemplate());
                if (StringUtils.isEmpty(entity.getIp())) {
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
        template.setDataSource(useMianDatabaseSource ? mianDataSource : this.dataSource);
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
    private String getDbConnectUrl(DbInfo config, DbInfo entity) {
        String urlTemplate = "";
        if (StringUtils.isEmpty(entity.getConnectStr())) {
            urlTemplate = config.getUrlTemplate();
            urlTemplate = urlTemplate.replace(IP, entity.getIp());
            urlTemplate = urlTemplate.replace(PORT, String.valueOf(entity.getPort()));
            urlTemplate = urlTemplate.replace(END_PARAM, entity.getEndParam());
        } else {
            urlTemplate = entity.getConnectStr();
        }
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
        printLog(sql, params);
        JdbcTemplate template = getJdbcTemplate();
        return (T) template.queryForObject(sql, requiredType, params);

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
        printLog(sql, params);
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
        printLog(sql, params);
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
        printLog(sql, params);
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
        printLog(sql, params);
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
        printLog(sql, params);
        JdbcTemplate template = getJdbcTemplate();
        return template.queryForList(sql, params);
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
        PageQueryInfo queryInfo = getPageQueryInfo(page, rows, sql);
        resMap.put("total", querySingleTypeResult(queryInfo.getCountSql(), int.class, params));
        List<Map<String, Object>> pageData = queryListMap(queryInfo.getPageSql(), params);
        resMap.put("pageData", isResultString ? resultConvert(pageData) : pageData);
        resMap.put("page", page);
        resMap.put("rows", rows);
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
        printLog(sql, params);
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
    public  String beginTransaction() throws Exception {
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
    public  void commitTransaction(String transactionId) {
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
    public  void rollbackTransaction(String transactionId) {
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
        printLog(sql);
        JdbcTemplate template = getJdbcTemplate();
        template.execute(sql);
    }
}
