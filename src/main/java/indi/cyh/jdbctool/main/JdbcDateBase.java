package indi.cyh.jdbctool.main;

import indi.cyh.jdbctool.modle.*;
import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import indi.cyh.jdbctool.tool.StringTool;
import indi.cyh.jdbctool.toolinterface.FieldColumn;
import indi.cyh.jdbctool.toolinterface.TableName;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import sun.misc.BASE64Encoder;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

    private static DataSourceBuilder builder = DataSourceBuilder.create();

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

    public DbInfo dbInfo;

    DataSource dataSource;


    private DbConfig defaultConfig;

    /**
     * @param entity
     * @author cyh
     * @description 根据参数初始化 数据源
     * @date 2020/4/11 9:18
     **/
    public JdbcDateBase(DbInfo entity, DbConfig config) throws Exception {
        this.defaultConfig = config;
        boolean isUserMainDbConfig = entity == null;
        //使用数据库默认主配或者读取次配置才去读取配置生成数据源  否则直接使用实例中给出的相应参数生成数据源
        if (isUserMainDbConfig || config.isReadConfig()) {
            //检查是否加载到配置
            if (checkDefaultConfig()) {
                if (isUserMainDbConfig) {
                    entity = new DbInfo();
                    //加载主配
                    loadingMainDbConfig(entity);
                } else {
                    //把配置结合参数转换未实体类
                    setDbInfo(entity);
                }
            } else {
                throw new Exception("未加载到数据库默认配置,请检查配置!");
            }
        }
        //根据实体生成数据源
        loadDatebase(entity);
    }


    private void loadingMainDbConfig(DbInfo entity) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        String configFileName = StringTool.isEmpty(defaultConfig.getConfigFileName()) ? DEFAULT_CONFIG_NAME : defaultConfig.getConfigFileName();
        yaml.setResources(new ClassPathResource(configFileName));
        Properties properties = yaml.getObject();
        entity.setConnectStr((String) properties.get(MAIN_DB_URL_PATH));
        entity.setLogoinName((String) properties.get(MAIN_DB_URL_USERNAME_PATH));
        entity.setPwd((String) properties.get(MAIN_DB_URL_PWD_PATH));
        entity.setDriverClassName((String) properties.get(MAIN_DB_URL_DRIVER_PATH));
    }

    private void loadDatebase(DbInfo dbInfo) {
        wl.lock();
        rl.lock();
        builder.driverClassName(dbInfo.getDriverClassName());
        builder.url(dbInfo.getConnectStr());
        builder.username(dbInfo.getLogoinName());
        builder.password(dbInfo.getPwd());
        this.dbInfo = dbInfo;
        this.dataSource = builder.build();
        rl.unlock();
        wl.unlock();
    }

    private boolean checkDefaultConfig() {
        return defaultConfig != null;
    }

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

    public JdbcTemplate getJdbcTemplate() {
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(this.dataSource);
        return template;
    }

    private String getDbConnectUrl(DbInfo config, DbInfo entity) throws Exception {
        String urlTemplate = config.getUrlTemplate();
        urlTemplate = urlTemplate.replace(IP, entity.getIp());
        urlTemplate = urlTemplate.replace(PORT, String.valueOf(entity.getPort()));
        urlTemplate = urlTemplate.replace(END_PARAM, entity.getEndParam());
        return urlTemplate;
    }

    /**
     * 查询
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
        return (T) template.queryForObject(sql, requiredType, params);
    }

    public <T> List<T> queryList(String sql, Class<T> requiredType, @Nullable Object... params) {
        Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(requiredType);
        JdbcTemplate template = getJdbcTemplate();
        return template.query(sql, new JdbcRowMapper<T>(requiredType), params);
    }
    public Map queryForMap(String sql, @Nullable Object... params) {
        JdbcTemplate template = getJdbcTemplate();
        return template.queryForMap(sql, params);
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

    public List<String> queryListString(String sql, @Nullable Object... params) {
        return getJdbcTemplate().query(sql, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }

    /**
     * 查询分页数据
     *
     * @param serviceSql 原始sql
     * @param page       页数
     * @param rows       每页行数
     * @return java.lang.Object
     * @author CYH
     * @date 2020/4/28 0028 16:03
     **/
    public Object queryPageDate(String serviceSql, Integer page, Integer rows, @Nullable Object... params) throws Exception {
        Map<String, Object> resMap = new HashMap<>();
        PageQueryInfo queryInfo = getPageQueryInfo(page, rows, serviceSql);
        resMap.put("total", queryOneRow(queryInfo.getCountSql(), int.class, params));
        List<Map<String, Object>> pageDate = queryListMap(queryInfo.getPageSql(), params);
        resMap.put("pageDate", resultConvert(pageDate));
        resMap.put("page", page);
        resMap.put("rows", rows);
        return resMap;
    }

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
     * 对象转数组
     *
     * @param obj
     * @return
     */
    public byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 有限制的ListMap 查询
     *
     * @param serviceSql 查询sql
     * @param MaxCount   最大数量
     * @param params     参数
     * @return java.lang.Object
     * @author CYH
     * @date 2020/4/30 0030 10:26
     **/
    public Object queryListMapByLimit(String serviceSql, int MaxCount, @Nullable Object... params) throws Exception {
        PageQueryInfo queryInfo = getPageQueryInfo(1, MaxCount, serviceSql);
        return resultConvert(queryListMap(queryInfo.getPageSql(), params));
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
            if (!matcherDistinct.find() && !sqlSelectCols.trim().toLowerCase().equals("*")) {
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
//
//    /**
//     * 查询获取list<T>
//     *
//     * @param sql          sql
//     * @param requiredType 返回类型
//     * @param params       参数
//     * @return java.util.List<T>
//     * @author cyh
//     * 2020/4/11 18:13
//     **/
//    public <T> List<T> queryForList(String sql, Class<T> requiredType, Object... params) {
//        JdbcTemplate template = getJdbcTemplate();
//        List<T> result = new ArrayList<T>();
//        template.query(sql, params, rs -> {
//            try {
//                // 字段名称
//                List<String> columnNames = new ArrayList<String>();
//                //列的类型和属性信息的对象
//                ResultSetMetaData meta = rs.getMetaData();
//                int num = meta.getColumnCount();
//                for (int i = 0; i < num; i++) {
//                    columnNames.add(meta.getColumnLabel(i + 1));
//                }
//                // 设置值
//                do {
//                    T obj = requiredType.getConstructor().newInstance();
//                    for (int i = 0; i < num; i++) {
//                        // 获取值
//                        Object value = rs.getObject(i + 1);
//                        // table.column形式的字段去掉前缀table.
//                        String columnName = resolveColumn(columnNames.get(i));
//                        // 下划线转驼峰
//                        String property = CamelCaseUtils.toCamelCase(columnName);
//                        // 复制值到属性，这是spring的工具类
//                        BeanUtils.copyProperty(obj, property, value);
//                    }
//                    result.add(obj);
//                } while (rs.next());
//            } catch (Exception e) {
//                throw new QueryException(e);
//            }
//        });
//
//
//    }
}
