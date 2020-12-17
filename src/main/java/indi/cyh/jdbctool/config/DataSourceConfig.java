package indi.cyh.jdbctool.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读取druid 配置
 *
 * @ClassName DataSourceConfig
 * @Description TODO
 * @Author gm
 * @Date 2020/12/17 21:44
 */
public class DataSourceConfig {
    /**
     * 初始连接数
     */
    public static final int DEFAULT_INITIALSIZE = 3;

    /**
     * 最小连接池数量
     */
    public static final int DEFAULT_MINIDLE = 5;

    /**
     * 最大连接数
     */
    public static final int DEFAULT_MAXACTIVE = 20;

    /**
     * 从连接池获取连接等待超时的时间
     */
    public static final int DEFAULT_MAXWAIT = 10000;

    /**
     * 配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
     * 检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
     * 2.在minIdle以内的不处理。
     */
    public static final int DEFAULT_TIMEBETWEENEVICTIONRUNSMILLIS = 600000;
    /**
     * 配置一个连接在池中最大空闲时间，单位是毫秒
     */
    public static final int DEFAULT_MINEVICTABLEIDLETIMEMILLIS = 300000;

    /**
     * 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。
     * 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
     */
    public static final int DEFAULT_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE = 20;

    public static final int DEFAULT_REMOVEABANDONEDTIMEOUT = 80;
    /**
     * 根据自身业务及事务大小来设置
     */
    public static final String DEFAULT_CONNECTIONPROPERTIES = "oracle.net.CONNECT_TIMEOUT=6000;oracle.jdbc.ReadTimeout=180000";
    /**
     * 设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查
     */
    public static final boolean DEFAULT_TESTWHILEIDLE = false;
    /**
     * 设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查
     */
    public static final boolean DEFAULT_TESTONBORROW = false;
    /**
     * 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查
     */
    public static final boolean DEFAULT_TESTONRETURN = false;
    /**
     * 打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料
     */
    public static final boolean DEFAULT_POOLPREPAREDSTATEMENTS = false;
    /**
     * 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。
     */
    public static final boolean DEFAULT_REMOVEABANDONED = true;

    /**
     * 打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性.
     */
    public static final boolean DEFAULT_KEEPALIVE = true;


    //静态控制锁
    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl;
    private static final ReentrantReadWriteLock.WriteLock wl;

    static {
        //锁赋值
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
    }

    public static void loadConfig(JSONObject config) {
        if (null != config) {


        }
    }

    public static void setConfig(DruidDataSource dataSource) {
        //配置初始化大小、最小、最大
        dataSource.setInitialSize(DEFAULT_INITIALSIZE);
        dataSource.setMinIdle(DEFAULT_MINIDLE);
        //最大活跃数
        dataSource.setMaxActive(DEFAULT_MAXACTIVE);
        //配置从连接池获取连接等待超时的时间
        dataSource.setMaxWait(DEFAULT_MAXWAIT);
        //配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
        //检测时:1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
        // 2.在minIdle以内的不处理。
        dataSource.setTimeBetweenEvictionRunsMillis(DEFAULT_TIMEBETWEENEVICTIONRUNSMILLIS);
        // 配置一个连接在池中最大空闲时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(DEFAULT_MINEVICTABLEIDLETIMEMILLIS);
        //  验证连接语句
        // dataSource.setValidationQuery(false);
        //设置从连接池获取连接时是否检查连接有效性，true时，如果连接空闲时间超过minEvictableIdleTimeMillis进行检查，否则不检查;false时，不检查
        dataSource.setTestWhileIdle(DEFAULT_TESTWHILEIDLE);
        //设置从连接池获取连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        dataSource.setTestOnBorrow(DEFAULT_TESTONBORROW);
        // 设置往连接池归还连接时是否检查连接有效性，true时，每次都检查;false时，不检查
        dataSource.setTestOnReturn(DEFAULT_TESTONRETURN);
        //打开PSCache，并且指定每个连接上PSCache的大小，Oracle等支持游标的数据库，打开此开关，会以数量级提升性能，具体查阅PSCache相关资料
        dataSource.setPoolPreparedStatements(DEFAULT_POOLPREPAREDSTATEMENTS);
        //
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(DEFAULT_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE);
        //根据自身业务及事务大小来设置
        dataSource.setConnectionProperties(DEFAULT_CONNECTIONPROPERTIES);
        //打开后，增强timeBetweenEvictionRunsMillis的周期性连接检查，minIdle内的空闲连接，每次检查强制验证连接有效性. 参考：https://github.com/alibaba/druid/wiki/KeepAlive_cn
        dataSource.setKeepAlive(DEFAULT_KEEPALIVE);
        // 连接泄露检查，打开removeAbandoned功能 , 连接从连接池借出后，长时间不归还，将触发强制回连接。回收周期随timeBetweenEvictionRunsMillis进行，如果连接为从连接池借出状态，并且未执行任何sql，并且从借出时间起已超过removeAbandonedTimeout时间，则强制归还连接到连接池中。
        dataSource.setRemoveAbandoned(DEFAULT_REMOVEABANDONED);

        dataSource.setRemoveAbandonedTimeout(DEFAULT_REMOVEABANDONEDTIMEOUT);
    }
}
