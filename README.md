# JdbcTool

基于[idea](https://www.jetbrains.com/?from=jdbcTool) 开发
#### 介绍

依靠spring-jdbc的动态多源数据库操作工具

#### 特性

- 支持主流关系型数据库数据库(默认mysql、Oracle、postgres,可根据自身需求添加jdbcConnection模板)
- 动态加载数据池
- 多数据源
- 分页查询
- 集成duird(若有需要可在项目中添加druid监控页面配置)

#### 使用说明

在classpath下wood.json中以json格式配置数据库信息,也可在环境变量中以参数configFile指定文件名(ex:java -jar demo.jar --configFile=wood.json)

- json配置文件示例

```
{
  //是否调试模式 控制控制台日志打印输出
  "isDebugger": true,
  //多数据源配置
  "dataBaseConfig": [
    {
      "sourceName": "mainDb",
      "type": "mysql",
      "ip": "106.52.167.158",
      "port": 3306,
      "loginName": "singlewood",
      "pwd": "singlewood",
      "endParam": "singlewood?serverTimezone=UTC"
    }
  ],
  //jdbc模板为运行中动态生成数据源提供模板支持
  "dataBaseTemplate": [
    {
      "type": "mysql",
      "driverClassName": "com.mysql.cj.jdbc.Driver",
      "port": 3306,
      "jdbcTemplate": "jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}"
    },
    {
      "type": "oracle",
      "driverClassName": "oracle.jdbc.driver.OracleDriver",
      "port": 1521,
      "jdbcTemplate": "jdbc:oracle:thin:@{{IP}}:{{PORT}}/{{END_PARAM}}"
    },
    {
      "type": "postgres",
      "driverClassName": "org.postgresql.Driver",
      "port": 5432,
      "jdbcTemplate": "jdbc:postgresql://{{IP}}:{{PORT}}/{{END_PARAM}}"
    }
  ],
  //通过配置druidConfig属性自定义默认druid配置 更多druid参数可自行百度
  "druidConfig": {
    "initialSize": 22,
    "maxActive": 30
  }
}

```

- 实体类示例

```
@TableName("ST_PARM")
@PrimaryKey("PARM_ID")
public class StParm implements Serializable {

    /**
     * 参数编号
     */
    @FieldColumn("PARM_ID")
    private String parmId;
    /**
     * 参数分类
     */
    @FieldColumn("CATEGORY")
    private String category;
}
```

### 使用示例

1. 初始化数据连接

```
//1.使用默认主配
 JdbcDataBase db = DataSourceFactory.getJdbcDataBase();
//2.使用配置名匹配加载
 JdbcDataBase db = DataSourceFactory.getJdbcDataBase("sourceName");
//3.依赖模板方式(可以通过ConfigCenter.addDataBaseTemplate动态添加模板)
 DbInfo info = new DbInfo() {{
    setSourceName("testDb");
    setType("mysql");
    setIp("*.*.*.*");
    setPort(3306);
    setLoginName("*");
    setPwd("*");
    setEndParam("singlewood?serverTimezone=UTC");
}};
JdbcDataBase db = DataSourceFactory.getJdbcDataBaseByInfo(info, true);
//4.不依赖任何配置模式方式
DbInfo  info = new DbInfo() {{
    setConnectStr("jdbc:mysql://*.*.*.*:3306/singlewood?serverTimezone=UTC");
    setDriverClassName("com.mysql.cj.jdbc.Driver");
    setLoginName("*");
    setPwd("*");
}};
JdbcDataBase db = DataSourceFactory.getJdbcDataBaseByInfo(info, false);
```

2. 具体使用

- 新增单个实体(配合带本工具注解的实体类使用)

```
db.insert(BsDiary.class,new BsDiary(){{
    setDHead("head");
    ....
}});
            
```

- 关联id单个删除(配合带本工具注解的实体类使用)

```
db.delectbyId(BsDiary.class,"1");
```

- 关联id多个删除(配合带本工具注解的实体类使用)

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

- 单一实体数据查询(配合带本工具注解的实体类使用)

```
db.queryOneRow("select  * from bs_diary  where d_diaryId=?",BsDiary.class,"42");
```

- 多行实体数据查询(配合带本工具注解的实体类使用)

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













