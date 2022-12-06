package indi.cyh.sailjdbc.tool;

import indi.cyh.sailjdbc.annotation.FieldColumn;
import indi.cyh.sailjdbc.annotation.PrimaryKey;
import indi.cyh.sailjdbc.annotation.TableName;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

/**
 * 实体类处理工具
 *
 * @author CYH
 * @date 2020/3/31 0031 9:44
 */
public class EntityTool {
    /**
     * 获取实体类表名
     *
     * @param type
     * @return java.lang.String
     * @author CYH
     * @date 2020/5/27 0027 10:46
     **/
    public static <T> String getTableName(Class<T> type) {
        try {
            return type.getAnnotation(TableName.class).value();
        } catch (Exception e) {
            LogTool.handleExceptionLog("获取实体类(%s)表名异常", true, e, type.toString());
            return "";
        }
    }

    /**
     * 获取实体类主键
     *
     * @param type
     * @return java.lang.String
     * @author CYH
     * @date 2020/5/27 0027 10:46
     **/
    public static <T> String getEntityPrimaryField(Class<T> type) {
        try {
            return type.getAnnotation(PrimaryKey.class).value();
        } catch (Exception e) {
            LogTool.handleExceptionLog("获取实体类(%s)主键异常", true, e, type.toString());
            return "";
        }
    }

    /**
     * 获取实体类上注解类的所有字段名
     *
     * @param type
     * @return java.util.List<java.lang.String>
     * @author CYH
     * @date 2020/5/27 0027 11:35
     **/
    public static <T> List<String> getEntityFieldList(Class<T> type) {
        List<String> list = new ArrayList<>();
        try {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(FieldColumn.class) != null && !StringTool.isEmpty(field.getAnnotation(FieldColumn.class).value())) {
                    list.add(field.getAnnotation(FieldColumn.class).value());
                }
            }
        } catch (Exception e) {
            LogTool.handleExceptionLog("获取实体类(%s)字段异常", true, e, type.toString());
        }
        return list;
    }

    /**
     * 实体类字段对应表字段
     *
     * @param type
     * @return java.util.List<java.lang.String>
     * @author CYH
     * @date 2020/5/27 0027 11:35
     **/
    public static <T> Map<String, String> getEntityFieldColumnMap(Class<T> type) {
        Map<String, String> map = new HashMap<>();
        try {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(FieldColumn.class) != null && !StringTool.isEmpty(field.getAnnotation(FieldColumn.class).value())) {
                    map.put(field.getName(), field.getAnnotation(FieldColumn.class).value());
                }
            }
        } catch (Exception e) {
            LogTool.handleExceptionLog("获取实体类(%s)对应表字段异常", true, e, type.toString());
        }
        return map;
    }

    /**
     * 获取类型的字段名
     *
     * @param type
     * @return java.util.List<java.lang.String>
     * @author cyh
     * 2020/7/14 22:34
     **/
    public static <T> List<String> getEntityFieldName(Class<T> type) {
        Field[] fields = type.getDeclaredFields();
        List<String> filedNameArr = new ArrayList<>();
        for (Field field : fields) {
            filedNameArr.add(field.getName());
        }
        return filedNameArr;
    }

    public static <T> String getPrimaryFeldValue(Class<? super T> entityType, T t) throws IllegalAccessException, NoSuchFieldException {
        String primaryKeyName = getPrimaryFeldName(entityType);
        Field field = entityType.getDeclaredField(primaryKeyName);
        field.setAccessible(true);
        return field.get(entityType).toString();
    }

    public static <T> String getPrimaryFeldName(Class<? super T> entityType) {
        //获取主键字段
        String primaryField = EntityTool.getEntityPrimaryField(entityType);
        //实体类字段对应表字段
        Map<String, String> fileToTable = EntityTool.getEntityFieldColumnMap(entityType);
        //主键字段名称
        return fileToTable.keySet().stream().filter(p -> {
            return fileToTable.get(p).equals(primaryField);
        }).findFirst().orElse(null);
    }

    /**
     * 设置主键值
     *
     * @param entityType
     * @param t
     * @param value
     * @return void
     * @author CYH
     * @date 2022/7/1 13:48
     **/
    public <T> void setPrimaryFeldValue(Class<? super T> entityType, T t, String value) throws NoSuchFieldException, IllegalAccessException {
        String primaryKeyName = getPrimaryFeldName(entityType);
        Field field = entityType.getDeclaredField(primaryKeyName);
        field.setAccessible(true);
        field.set(t, value);
    }

    /**
     * 设置对象属性值
     *
     * @param t
     * @param columName
     * @param value
     * @return void
     * @author CYH
     * @date 2022/7/1 13:49
     **/
    public static <T> void setColumValue(T t, String columName, Object value) throws IllegalAccessException, NoSuchFieldException, SQLException, IOException {
        Field field = t.getClass().getDeclaredField(columName);
        field.setAccessible(true);
        value = DataConvertTool.convertObject(field.getType(), value);
        field.set(t, value);
    }

    /**
     * 使用ObjectStream序列化实现深克隆
     *
     * @return Object obj
     */
    public <T extends Serializable> T deepClone(T t) throws CloneNotSupportedException {
        // 保存对象为字节数组
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
                out.writeObject(t);
            }

            // 从字节数组中读取克隆对象
            try (InputStream bin = new ByteArrayInputStream(bout.toByteArray())) {
                ObjectInputStream in = new ObjectInputStream(bin);
                return (T) (in.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            CloneNotSupportedException cloneNotSupportedException = new CloneNotSupportedException();
            e.initCause(cloneNotSupportedException);
            throw cloneNotSupportedException;
        }
    }
}
