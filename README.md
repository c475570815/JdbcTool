# JdbcTool

#### 介绍
工具依靠spring-jdbc的多源操作数据库工具结合springboot使用

#### 使用说明
目前有三种方式生成数据源
   1.  在配置文件中配置不同类型的jdbcConnection模板,通过DbConfig实体类给出相关参数生成数据源

```
@Autowired
    private DbConfig config;
    DbInfo dbInfo = new DbInfo() {{
            setDbType("mysql");
            setIp("*.*.*.*");
            setPort(3306);
            setLogoinName("***");
            setPwd("***");
            setEndParam("singlewood");
        }};
    JdbcDateBase db = new JdbcDateBase(dbInfo, config);
```

   2.  动态注入数据库连接串和使用的数据库驱动生成数据源(DbConfig 参数可为null)

```
@Autowired
    private DbConfig config;
    DbInfo dbInfo = new DbInfo() {{
                setConnectStr("jdbc:mysql://106.52.167.158:3306/singlewood");
                setDriverClassName("com.mysql.cj.jdbc.Driver");
                setLogoinName("root");
                setPwd("cyh123321");
            }};
            JdbcDateBase db = new JdbcDateBase(dbInfo, config);
```


   3.  在实例化JdbcDateBase时,传入DbInfo为null则以配置文件中主数据库配置(spring.datasource)参数来生成数据源(DbConfig 参数可为null)

```
@Autowired
    private DbConfig config;
    JdbcDateBase db = new JdbcDateBase(null, config);
```

- 主数据库配置示例

```
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/singlewood?serverTimezone=UTC
    username: singlewood
    password: singlewood
    driver-class-name: com.mysql.cj.jdbc.Driver
```
若不想配置默认库可以在入口使用注解

```
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
```



- 其他类型数据库配置(config-file-name参数用于寻找主配文件)

```
db-config:
  # 配置生成connectStr模板
  defalut-config-list:
    - {db-type: mysql,driver-class-name: com.mysql.cj.jdbc.Driver, port: 3306, url-template: 'jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}'}
    - {db-type: oracle,driver-class-name: oracle.jdbc.driver.OracleDriver, port: 1521, url-template: 'jdbc:oracle:thin:@{{IP}}:{{PORT}}/{{END_PARAM}}'}
    - {db-type: postgres,driver-class-name: org.postgresql.Driver, port: 3306, url-template: 'jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}'}
  # 指定读取其他配置文件  默认为 application.yml
  config-file-name: application.yml
  # 是否为调试模式 默认为true
  debugger: false
```

- 作为jar包被其他项目依赖时在classpath下META-INF/spring.factories 加入以下内容

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
indi.cyh.jdbctool.modle.DbConfig
```
### 使用示例
1. 初始化数据连接

```
    @Autowired
    private DbConfig config;
    DbInfo dbInfo = new DbInfo() {{
            setDbType("mysql");
            setIp("*.*.*.*");
            setPort(3306);
            setLogoinName("***");
            setPwd("***");
            setEndParam("singlewood");
        }};
    JdbcDateBase db = new JdbcDateBase(dbInfo, config);
```
2. 具体使用
- 新增单个实体

```
            db.insert(BsDiary.class,new BsDiary(){{
                setDHead("head");
                ....
            }});
            
```
- 关联id单个删除

```
            db.delectbyId(BsDiary.class,"1");
```
- 关联id多个删除
```
            db.delectbyIds(BsDiary.class, new ArrayList<>() {{
                add("1");
                add("2");
                ...
            }});
```
- 单一简单类型数据查询

```
            db.querySingleTypeResult("select  head  from bs_diary where d_diaryId=?",String.class,"42");
```
- 多行简单类型数据查询

```
            db.querySingleTypeList("select  head  from bs_diary ",String.class);
```
- 单一实体数据查询

```
            db.queryOneRow("select  * from bs_diary  where d_diaryId=?",BsDiary.class,"42");
```
- 多行实体数据查询

```
            db.queryList("select  *  from bs_diary ",BsDiary.class);
```
- Map查询

```
            db.queryForMap("select  *  from bs_diary  where d_diaryId=?","42");
```
- list<Map>查询

```
            db.queryListMap("select  *  from bs_diary ");
```
- 分页查询

```
            db.queryPageDate("select  *  from bs_diary",1,10,true);
```
- 根据id更新
```
            BsDiary diary=db.findRowById(BsDiary.class,"42");
            diary.setDHead("update");
            db.updateById(BsDiary.class,diary);
```
- 事务

```
            JdbcDateBase db = new JdbcDateBase(dbInfo, config);
            String transactionId = db.beginTransaction();
            try {
                // 新增单个实体
                db.insert(BsDiary.class, new BsDiary() {{
                    setDHead("head");
                }});
                throw  new Exception("eee");
               // db.commitTransaction(transactionId);
            }catch (Exception e){
                db.rollbackTransaction(transactionId);
            }
```

......待更新













