package indi.cyh.jdbctool.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Method;

/**
 * 读取druid 配置
 *
 * @ClassName DataSourceConfig
 * @Description TODO
 * @Author gm
 * @Date 2020/12/17 21:44
 * <p>
 * 2021-02-23 改变一些变量的作用域 和 配置默认数据源和配置覆盖参数的一些操作
 * ----> 1. 参数作用域 public 到 private
 * ----> 2. 删除defaultDataSource变量和loadConfig方法改为getDataSource提供服务
 */
public class DataSourceConfig {
    /**
     * 初始连接数
     */
    private static int DEFAULT_INITIALSIZE = 3;

    /**
     * 最小连接池数量
     */
    private static int DEFAULT_MINIDLE = 5;

    /**
     * 最大连接数
     */
    private static int DEFAULT_MAXACTIVE = 20;

    /**
     * 从连接池获取连接等待超时的时间
     */
    private static int DEFAULT_MAXWAIT = 10000;

    /**
     * 配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
     * 检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
     * 2.在minIdle以内的不处理。
     */
    private static int DEFAULT_TIMEBETWEENEVICTIONRUNSMILLIS = 600000;
    /**
     * 配置一个连接在池中最大空闲时间，单位是毫秒
     */
    private static int DEFAULT_MINEVICTABLEIDLETIMEMILLIS = 300000;

    /**
     * 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。
     * 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
     */
    private static int DEFAULT_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE = 20;

    private static int DEFAULT_REMOVEABANDONEDTIMEOUT = 80;
    /**
     * 根据自身业务及事务大小来设置
     */
    private static String DEFAULT_CONNECTIONPROPERTIES = "oracle.net.CONNECT_TIMEOUT=6000;oracle.jdbc.ReadTimeout=180000";
    /**
     * 设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查
     */
    private static boolean DEFAULT_TESTWHILEIDLE = false;
    /**
     * 设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查
     */
    private static boolean DEFAULT_TESTONBORROW = false;
    /**
     * 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查
     */
    private static boolean DEFAULT_TESTONRETURN = false;
    /**
     * 打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料
     */
    private static boolean DEFAULT_POOLPREPAREDSTATEMENTS = false;
    /**
     * 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。
     */
    private static boolean DEFAULT_REMOVEABANDONED = true;

    /**
     * 打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性.
     */
    private static boolean DEFAULT_KEEPALIVE = true;

    /**
     * 方法功能描述: 获取DataSource
     *
     * @param druid druid 配置文件
     * @return DruidDataSource 数据源
     */
    public static DruidDataSource getDataSource(JSONObject druid) {
        DruidDataSource druidDataSource;
        try {
            druidDataSource = initDefaultDataSource();
            coverConfig(druidDataSource, druid);
        } catch (Exception ex) {
            ex.printStackTrace();
            druidDataSource = null;
        }
        return druidDataSource;
    }

    /**
     * 方法功能描述: 初始化数据源默认参数
     */
    private static DruidDataSource initDefaultDataSource() {
        DruidDataSource defaultDataSource = new DruidDataSource();
        //配置初始化大小、最小、最大
        defaultDataSource.setInitialSize(DEFAULT_INITIALSIZE);

        defaultDataSource.setMinIdle(DEFAULT_MINIDLE);
        //最大活跃数
        defaultDataSource.setMaxActive(DEFAULT_MAXACTIVE);
        //配置从连接池获取连接等待超时的时间
        defaultDataSource.setMaxWait(DEFAULT_MAXWAIT);
        //配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
        //检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
        // 2.在minIdle以内的不处理。
        defaultDataSource.setTimeBetweenEvictionRunsMillis(DEFAULT_TIMEBETWEENEVICTIONRUNSMILLIS);
        // 配置一个连接在池中最大空闲时间，单位是毫秒
        defaultDataSource.setMinEvictableIdleTimeMillis(DEFAULT_MINEVICTABLEIDLETIMEMILLIS);
        //  验证连接语句
        // dataSource.setValidationQuery(false);
        //设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查
        defaultDataSource.setTestWhileIdle(DEFAULT_TESTWHILEIDLE);
        //设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        defaultDataSource.setTestOnBorrow(DEFAULT_TESTONBORROW);
        // 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        defaultDataSource.setTestOnReturn(DEFAULT_TESTONRETURN);
        //打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料
        defaultDataSource.setPoolPreparedStatements(DEFAULT_POOLPREPAREDSTATEMENTS);
        //
        defaultDataSource.setMaxPoolPreparedStatementPerConnectionSize(DEFAULT_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE);
        //根据自身业务及事务大小来设置
        defaultDataSource.setConnectionProperties(DEFAULT_CONNECTIONPROPERTIES);
        //打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性. 参考：https://github.com/alibaba/druid/wiki/KeepAlive_cn
        defaultDataSource.setKeepAlive(DEFAULT_KEEPALIVE);
        // 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。
        defaultDataSource.setRemoveAbandoned(DEFAULT_REMOVEABANDONED);

        defaultDataSource.setRemoveAbandonedTimeout(DEFAULT_REMOVEABANDONEDTIMEOUT);
        return defaultDataSource;
    }

    /**
     * 方法功能描述: 复写默认配置
     *
     * @param druid 数据源参数根据配置文件复写
     * @return void
     */
    private static void coverConfig(DruidDataSource druidDataSource, JSONObject druid) throws Exception {
        if (druidDataSource == null) {
            throw new Exception("druid dataSource init fail.....");
        }
        if (null != druid) {
            try {
                druidDataSource = druid.toJavaObject(DruidDataSource.class);
            } catch (Exception e) {
                initDefaultDataSource();
                throw new Exception("读取druid配置失败,请检查配置:" + e.getMessage());
            }
        }
    }
}
