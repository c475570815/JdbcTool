package indi.cyh.jdbctool.core;

import com.alibaba.druid.pool.DruidDataSource;
import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.DbTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * DataSourceFactory
 *
 * @ClassName DataSourceFactory
 * @Description TODO
 * @Author gm
 * @Date 2020/7/16 20:40
 */
public class DataSourceFactory {

    //配置文件相关参数
    private static final String DEFAULT_CONFIG_NAME = "application.yml";
    private static final String MAIN_DB_URL_PATH = "spring.datasource.url";
    private static final String MAIN_DB_URL_USERNAME_PATH = "spring.datasource.username";
    private static final String MAIN_DB_URL_PWD_PATH = "spring.datasource.password";
    private static final String MAIN_DB_URL_DRIVER_PATH = "spring.datasource.driver-class-name";

    //jdbc连接生成相关参数
    private static final String IP = "{{IP}}";
    private static final String PORT = "{{PORT}}";
    private static final String END_PARAM = "{{END_PARAM}}";

    //静态控制锁
    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl;
    private static final ReentrantReadWriteLock.WriteLock wl;

    /**
     * 配置文件对象
     */
    private static final Properties properties;

    /**
     * 默认主配数据库
     */
    private static DruidDataSource mianDataSource;

    /**
     * 静态连接池
     */
    private static final Map<DbInfo, DruidDataSource> listDbSource = new LinkedHashMap<>();

    private static final Map<String, DbTemplate> defaultUrlTemplateMap = new LinkedHashMap<>();

    private static boolean debugger;


    static {
        //锁赋值
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
        //获取配置文件对象
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(DEFAULT_CONFIG_NAME));
        properties = yaml.getObject();
        //加载默认主配
        loadMainDbConfig();
        //配置默认jdbc-url模板
        loadDefaultUrlTemplate();
        debugger = properties.get("debugger") == null || properties.get("debugger").toString().equals("false");
    }

    /**
     * 配置默认jdbc-url模板
     *
     * @param
     * @return void
     * @author cyh
     * 2020/7/16 20:58
     **/
    private static void loadDefaultUrlTemplate() {
        DbTemplate mysql = new DbTemplate() {{
            setUrlTemplate("jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(3306);
            setDriverClassName("com.mysql.cj.jdbc.Driver");
            setDbType("mysql");
        }};
        DbTemplate oracle = new DbTemplate() {{
            setUrlTemplate("jdbc:oracle:thin:@{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(1521);
            setDriverClassName("oracle.jdbc.driver.OracleDriver");
            setDbType("oracle");
        }};
        DbTemplate postgres = new DbTemplate() {{
            setUrlTemplate("jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}");
            setPort(5432);
            setDriverClassName("org.postgresql.Driver");
            setDbType("postgres");
        }};
        defaultUrlTemplateMap.put("mysql", mysql);
        defaultUrlTemplateMap.put("oracle", oracle);
        defaultUrlTemplateMap.put("postgres", postgres);
    }

    /**
     * 添加模板
     *
     * @param
     * @return void
     * @author cyh
     * 2020/7/16 21:49
     **/
    public static void addDbTmplate(DbTemplate template) {
        boolean isExist = defaultUrlTemplateMap.keySet().contains(template.getDbType());
        defaultUrlTemplateMap.put(template.getDbType(), template);
        if (isExist) {
            System.out.println(template.getDbType() + "模板配置已被覆盖!");
        }
    }

    /**
     * 生成一个数据源
     *
     * @param entity
     * @param config
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author cyh
     * 2020/7/16 21:10
     **/
    private static DruidDataSource getDataSource(DbInfo entity, DbTemplate config) throws Exception {
        boolean hasTemplate = config != null;
        if (hasTemplate) {
            //使用给出的模板加载db连接实体类
            setDbInfo(entity, config);
        } else if (StringUtils.isNotEmpty(entity.getDbType()) && StringUtils.isEmpty(entity.getConnectStr())) {
            //把配置结合参数转换为db连接实体类
            setDbInfoByDefaultUrlTemplate(entity);
        }
        //根据实体生成数据源
        DruidDataSource dataSource = getExitDataSource(entity);
        if (dataSource == null) {
            dataSource = getNewDataSource(entity);
        }
        return dataSource;
    }

    /**
     * 获取db操作实体
     *
     * @param entity
     * @return indi.cyh.jdbctool.main.JdbcDataBase
     * @author cyh
     * 2020/7/16 21:46
     **/
    public static JdbcDataBase getDb(DbInfo entity) throws Exception {
        DruidDataSource source = getDataSource(entity, null);
        JdbcDataBase db = new JdbcDataBase(source);
        return db;
    }

    /**
     * 获取主配
     *
     * @param
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author cyh
     * 2020/7/16 21:13
     **/
    public static JdbcDataBase getMianDb() {
        JdbcDataBase db = new JdbcDataBase(mianDataSource);
        return db;
    }

    /**
     * 获取已存在的连接池
     *
     * @param dbInfo
     * @return com.alibaba.druid.pool.DruidDataSource
     * @author CYH
     * @date 2020/7/10 0010 16:00
     **/
    private static DruidDataSource getExitDataSource(DbInfo dbInfo) {
        rl.lock();
        for (DbInfo info : listDbSource.keySet()) {
            if (DbInfo.equals(info, dbInfo)) {
                System.out.println("获取到已有连接池:" + info.getConnectStr());
                System.out.println("目前连接池数量: " + listDbSource.size());
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
    private static void setDbInfoByDefaultUrlTemplate(DbInfo entity) throws Exception {
        for (DbTemplate config : defaultUrlTemplateMap.values()) {
            if (entity.getDbType().equals(config.getDbType())) {
                setDbInfo(entity, config);
                return;
            }
        }
        throw new Exception("不支持的数据源类型!");
    }

    private static void setDbInfo(DbInfo entity, DbTemplate config) {
        entity.setConnectStr(getDbConnectUrl(config, entity));
        entity.setDriverClassName(config.getDriverClassName());
        if (entity.getPort() == null) {
            entity.setPort(config.getPort());
        }
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
    private static String getDbConnectUrl(DbTemplate config, DbInfo entity) {
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
     * 加载默认主配
     *
     * @return void
     * @author cyh
     * 2020/5/28 21:55
     **/
    private static void loadMainDbConfig() {
        try {
            DbInfo entity = new DbInfo();
            if (properties.get(MAIN_DB_URL_PATH) != null) {
                entity.setConnectStr(String.valueOf(properties.get(MAIN_DB_URL_PATH)));
                entity.setLogoinName(String.valueOf(properties.get(MAIN_DB_URL_USERNAME_PATH)));
                entity.setPwd(String.valueOf(properties.get(MAIN_DB_URL_PWD_PATH)));
                entity.setDriverClassName(String.valueOf(properties.get(MAIN_DB_URL_DRIVER_PATH)));
                mianDataSource = getNewDataSource(entity);
            } else {
                throw new Exception(DEFAULT_CONFIG_NAME + "中没有配置数据库信息!");
            }
        } catch (Exception e) {
            System.out.println("主数据库加载失败! ---" + e.getMessage());
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
        dataSource.setName(dbInfo.getDbType() + "-" + dbInfo.getIp() + "-" + dbInfo.getLogoinName());
        //监控设置
        try {
            dataSource.setFilters("stat,wall,log4j2");
        } catch (Exception e) {

        }
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

        listDbSource.put(dbInfo, dataSource);

        System.out.println("目前连接池数量: " + listDbSource.size());
        return dataSource;
    }

    /**
     * 从配置文件获取是否是调试模式
     *
     * @param
     * @return boolean
     * @author cyh
     * 2020/7/16 21:14
     **/
    public static boolean getIsDebugger() {
        return debugger;
    }

    public static void setDebugger(boolean isDebugger) {
        debugger = isDebugger;
    }
}
