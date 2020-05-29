package indi.cyh.jdbctool.tool;

import indi.cyh.jdbctool.modle.ConvertType;
import org.postgresql.util.PGobject;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CYH
 * @date 2020/3/31 0031 9:44
 */
public class DataConvertTool {


    /**
     * 数据转换 byteToBase64
     *
     * @param columnDate
     * @return void
     * @author CYH
     * @date 2020/5/6 0006 16:15
     **/
    public static String byteToBase64(Object columnDate) {
        byte[] bytes = toByteArray(columnDate);
        return byte2Base64String(bytes).toString();
    }

    /**
     * 数据转换 pgObjectToString
     *
     * @param columnDate
     * @return java.lang.Object
     * @author CYH
     * @date 2020/5/6 0006 17:24
     **/
    public static String pgObjectToString(Object columnDate) {
        System.out.printf(columnDate.toString());
        return ((PGobject) columnDate).getValue();
    }


    /**
     * 数据转换 timestampToString
     *
     * @param columnDate
     * @return void
     * @author CYH
     * @date 2020/5/6 0006 17:03
     **/
    public static String timestampToString(Object columnDate) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(columnDate).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 对象转数组
     *
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 数据转换  byte2Base64String
     *
     * @param bytes
     * @return java.lang.Object
     * @author CYH
     * @date 2020/5/6 0006 17:05
     **/
    public static String byte2Base64String(byte[] bytes) {
        return new BASE64Encoder().encode(bytes);
    }

    /**
     * 获取一行中需要转换的列 与其转换方式
     *
     * @author CYH
     * @date 2020/5/6 0006 16:10
     **/
    public static void getConvertColumn(Map<String, Object> row, List<String> hasCheckColumnList, HashMap<String, ConvertType> converMap) {
        for (String key : row.keySet()) {
            if (row.get(key) != null && !hasCheckColumnList.contains(key)) {
                String name = row.get(key).getClass().getName();
                switch (name) {
                    case "java.sql.Timestamp":
                        converMap.put(key, ConvertType.TIMESTAMP_TO_STRING);
                        break;
                    case "org.postgresql.util.PGobject":
                        converMap.put(key, ConvertType.PGOBJECT_TO_STRING);
                        break;
                    case "[B":
                        converMap.put(key, ConvertType.BYTE_TO_BASE64);
                        break;
                    case "java.sql.Date":
                        converMap.put(key, ConvertType.TIMESTAMP_TO_STRING);
                        break;
                    default:
                        break;
                }
                hasCheckColumnList.add(key);
            }
        }
    }

    public static <T> Object convertObject(Class<T> targetType, Object res) {
        if (res == null) {
            return null;
        }
        String sourceTypeName = res.getClass().getName();
        String targetTypename = targetType.getName();
        switch (targetTypename) {
            case "java.lang.String":
                return convertToString(sourceTypeName, res);
            default:
                System.out.println("未处理目标类型:" + targetTypename);
                return res;
        }

    }

    private static String convertToString(String sourceTypeName, Object res) {
        switch (sourceTypeName) {
            case "java.lang.String":
                return res.toString();
            case "java.sql.Timestamp":
                return timestampToString(res);
            case "org.postgresql.util.PGobject":
                return pgObjectToString(res);
            case "[B":
                return byteToBase64(res);
            case "java.sql.Date":
                return timestampToString(res);
            case "java.math.BigDecimal":
                return res.toString();
            default:
                System.out.println("String转换未处理源类型:" + sourceTypeName);
        }
        return res.toString();
    }

}
