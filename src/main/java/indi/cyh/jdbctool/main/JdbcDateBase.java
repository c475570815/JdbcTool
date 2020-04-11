package indi.cyh.jdbctool.main;

import indi.cyh.jdbctool.modle.DbInfo;
import indi.cyh.jdbctool.modle.DbConfig;
import indi.cyh.jdbctool.tool.StringTool;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

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

    public DbInfo dbInfo;

    DataSource dataSource;


    private DbConfig defaultConfig;

    /**
     * @param entity
     * @return
     * @author cyh
     * @Description 根据参数初始化 数据源
     * @Date 2020/4/11 9:18
     **/
    public JdbcDateBase(DbInfo entity, DbConfig config) throws Exception {
        this.defaultConfig = config;
        boolean  isUserMainDbConfig=entity == null;
        //使用数据库默认主配或者读取次配置才去读取配置生成数据源  否则直接使用实例中给出的相应参数生成数据源
        if (isUserMainDbConfig||config.isReadConfig()) {
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
        builder.driverClassName(dbInfo.getDriverClassName());
        builder.url(dbInfo.getConnectStr());
        builder.username(dbInfo.getLogoinName());
        builder.password(dbInfo.getPwd());
        this.dbInfo = dbInfo;
        this.dataSource = builder.build();
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
}
