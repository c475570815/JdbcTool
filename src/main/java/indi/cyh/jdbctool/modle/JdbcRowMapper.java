package indi.cyh.jdbctool.modle;

import indi.cyh.jdbctool.tool.DataConvertTool;
import indi.cyh.jdbctool.tool.EntityTool;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author CYH
 * @date 2020/3/31 0031 9:44
 */
public class JdbcRowMapper<T> implements RowMapper<T> {

    private Class<T> type;

    private T getNewInstance() throws IllegalAccessException, InstantiationException {
        return type.newInstance();
    }

    public JdbcRowMapper(Class<T> type) {
        this.type = type;
    }

    @Override
    public T mapRow(ResultSet resultSet, int i) {
        try {
            T t = getNewInstance();
            Map<String, String> fieldColumnMap = EntityTool.getEntityFieldColumnMap(t.getClass());
            for (String column : fieldColumnMap.keySet()) {
                Field field = t.getClass().getDeclaredField(column);
                boolean accessFlag = field.isAccessible();
                field.setAccessible(true);
                Object res = resultSet.getObject(fieldColumnMap.get(column));
                Object lastConvertValue=DataConvertTool.convertObject(field.getType(), res);
                try {
                    field.set(t, lastConvertValue);
                }catch (Exception e){
                    System.out.println("转换有误：entity=" + t.getClass() + ", filedName = " +  column+ ", Value = " + res);
                }
                field.setAccessible(accessFlag);
            }
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
