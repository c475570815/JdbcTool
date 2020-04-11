package indi.cyh.jdbctool.modle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @ClassName DbConfig
 * @Description TODO
 * @Author gm
 * @Date 2020/4/11 8:54
 */
@Configuration
@ConditionalOnClass()
@ConfigurationProperties(prefix = "db-config")
public class DbConfig {
    List<DbInfo> defalutConfigList;
    boolean isReadConfig;
    String configFileName;

    public List<DbInfo> getDefalutConfigList() {
        return defalutConfigList;
    }

    public void setDefalutConfigList(List<DbInfo> defalutConfigList) {
        this.defalutConfigList = defalutConfigList;
    }

    public boolean isReadConfig() {
        return isReadConfig;
    }

    public void setReadConfig(boolean readConfig) {
        isReadConfig = readConfig;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }
}
