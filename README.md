# JdbcTool

#### 介绍
依靠spring-jdbc的动态多源数据库操作工具

#### 特性

- 支持主流关系型数据库数据库(默认mysql、Oracle、postgres,可根据自身需求添加jdbcConnection模板)
- 动态加载数据池
- 多数据源
- 分页查询
- 集成duird(若有需要可在项目中添加druid监控页面配置)


#### 使用说明
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



- jdbcConnection模板示例

必须包含{{IP}}、{{PORT}}、{{END_PARAM}}作为生成jdbcConnection的占位符
```
jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}
```

### 使用示例
1. 初始化数据连接

```
            //1.使用默认主配
             JdbcDataBase db = DataSourceFactory.getMianDb();

            //2.给出必要参数 依赖默认模板生成
            JdbcDataBase db =DataSourceFactory.getDb(new DbInfo(){{
                setDbType("mysql");
                setPort(3306);
                setLogoinName("***");
                setPwd("***");
                setIp("***");
                setEndParam("singlewood");
            }});

           // 3.给出url  不依赖模板
            JdbcDataBase db =DataSourceFactory.getDb(new DbInfo(){{
                setConnectStr("jdbc:mysql://***:3306/singlewood");
                setLogoinName("***");
                setPwd("***");
            }});

           // 4.给出新模板 依赖模板生成
            DataSourceFactory.addDbTmplate(new DbTemplate(){{
                setUrlTemplate("jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}");
                setPort(3306);
                setDriverClassName("com.mysql.cj.jdbc.Driver");
                setDbType("mysql-t");
            }});
            JdbcDataBase db =DataSourceFactory.getDb(new DbInfo(){{
                setDbType("mysql-t");
                setPort(3306);
                setLogoinName("root");
                setPwd("cyh123321");
                setIp("106.52.167.158");
                setEndParam("singlewood");
            }});
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
            db.queryPageData("select  *  from bs_diary",1,10,true);
```
- 根据id更新
```
            BsDiary diary=db.findRowById(BsDiary.class,"42");
            diary.setDHead("update");
            db.updateById(BsDiary.class,diary);
```
- 事务

```
            JdbcDataBase db = new JdbcDataBase(dbInfo, config);
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













