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
在classpath下wood.json中以json格式配置数据库信息,也可在环境变量中以参数configFile指定文件名(ex:java -jar demo.jar --configFile=wood.json)
- json配置文件示例
```
{
//是否调试模式 控制控制台日志打印输出
  "isDebugger": true,
  "dbConfig": {
  //多数据源配置
    "datasource": [
      {
        "sourceName": "mainDb",
        "type": "mysql",
        "ip": "*.*.*.*",
        "port": 3306,
        "loginName": "*",
        "pwd":  "*",
        "endParam": "singlewood?serverTimezone=UTC"
      }
    ],
    //jdbc模板为运行中动态生成数据源提供模板支持
    "templateList": [
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
    ]
  }
}

```

- 实体类示例
```
@TableName("ST_PARM")
@PrimaryField("PARM_ID")
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
    /**
     * 参数名称
     */
    @FieldColumn("NAME")
    private String name;
    /**
     * 参数默认值
     */
    @FieldColumn("DEFAULT_VALUE")
    private String defaultValue;
    /**
     * 参数当前值
     */
    @FieldColumn("NOW_VALUE")
    private String nowValue;
    /**
     * 参数运行值
     */
    @FieldColumn("RUN_VALUE")
    private String runValue;
    /**
     * 参数最小值
     */
    @FieldColumn("MIN_VALUE")
    private String minValue;
    /**
     * 参数最大值
     */
    @FieldColumn("MAX_VALUE")
    private String maxValue;
    /**
     * 参数格式
     */
    @FieldColumn("FORMAT")
    private String format;
    /**
     * 备注
     */
    @FieldColumn("NOTE")
    private String note;
    /**
     * 使用标志
     */
    @FieldColumn("STATE")
    private String state;
    /**
     * 排序
     */
    @FieldColumn("IDX")
    private String idx;
    /**
     * 系统编号
     */
    @FieldColumn("MIS_ID")
    private String misId;
}
```

### 使用示例
1. 初始化数据连接

```
            //1.使用默认主配
             JdbcDataBase db = DataSourceFactory.getMianDb();
            //2.使用配置名匹配加载
             db=  DataSourceFactory.getDbBySourceName("mainDb");
            //3.给出必要参数 依赖默认模板生成 
            JdbcDataBase db =DataSourceFactory.getDb(new DbInfo(){{
                setDbType("mysql");
                setPort(3306);
                setLogoinName("***");
                setPwd("***");
                setIp("***");
                setEndParam("singlewood");
            }});

           // 4.给出url  不依赖模板
            JdbcDataBase db =DataSourceFactory.getDb(new DbInfo(){{
                setConnectStr("jdbc:mysql://***:3306/singlewood");
                setLogoinName("***");
                setPwd("***");
            }});

           // 5.给出新模板 依赖模板生成
            DataSourceFactory.addDbTmplate(new DbTemplate(){{
                setUrlTemplate("jdbc:mysql://{{IP}}:{{PORT}}/{{END_PARAM}}");
                setPort(3306);
                setDriverClassName("com.mysql.cj.jdbc.Driver");
                setDbType("mysql-t");
            }});
            JdbcDataBase db =DataSourceFactory.getDb(new DbInfo(){{
                setDbType("mysql-t");
                setPort(3306);
                setLogoinName("***");
                setPwd("***");
                setIp("***");
                setEndParam("singlewood");
            }});
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













