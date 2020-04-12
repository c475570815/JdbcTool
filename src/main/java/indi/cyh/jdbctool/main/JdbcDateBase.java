package indi.cyh.jdbctool.main;

import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.DbConfig;
import indi.cyh.jdbctool.tool.StringTool;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private static DataSourceBuilder builder = DataSourceBuilder.create();

    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl;
    private static final ReentrantReadWriteLock.WriteLock wl;

    static {
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
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
        String configFileName = StringTool.isNotEmpty(defaultConfig.getConfigFileName()) ? defaultConfig.getConfigFileName() : DEFAULT_CONFIG_NAME;
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
                if (!StringTool.isNotEmpty(entity.getIp())) {
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
    public List<Map<String, Object>> queryListMap(String sql) {
        JdbcTemplate template = getJdbcTemplate();
        return template.queryForList(sql);
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
