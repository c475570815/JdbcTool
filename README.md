#author cyh
#Date 2020/4/11
####jdbc多源工具类####
#目前有三种方式生成数据源
#1.在配置文件中配置不同类型的数据库通过jdbc连接时的相关参数(read-config  为 true)
#2.直接代码注入数据库连接串和使用的数据库驱动(read-config  为 false)
#3.在实例化JdbcDateBase时 传入DbInfo参数为空则以配置文件中主数据库配置(spring.datasource)参数来生成数据源
#主数据库配置示例(*****使用这个依赖包配置文件中必须有一个主数据库配置)

```
spring:
  application:
    name: singlewood-provider
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/singlewood?serverTimezone=UTC
    username: singlewood
    password: singlewood
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#其他类型数据库配置

```
db-config:
  defalut-config-list:
    - {db-type: mysql,driver-class-name: com.mysql.cj.jdbc.Driver, port: 3306, url-template: 'jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}'}
    - {db-type: oracle,driver-class-name: oracle.jdbc.driver.OracleDriver, port: 1521, url-template: 'jdbc:oracle:thin:@{{IP}}:{{PORT}}/{{END_PARAM}}'}
    - {db-type: postgres,driver-class-name: org.postgresql.Driver, port: 3306, url-template: 'jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}'}
  read-config: false
```

#作为jar包被其他项目依赖时在classpath下META-INF/spring.factories 加入以下内容

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
indi.cyh.jdbctool.modle.DbConfig
```
