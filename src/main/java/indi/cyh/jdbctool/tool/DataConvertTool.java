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
    public static Object byteToBase64(Object columnDate) {
        byte[] bytes = toByteArray(columnDate);
        return byte2Base64String(bytes);
    }

    /**
     * 数据转换 pgObjectToString
     *
     * @param columnDate
     * @return java.lang.Object
     * @author CYH
     * @date 2020/5/6 0006 17:24
     **/
    public static Object pgObjectToString(Object columnDate) {
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
    public static Object timestampToString(Object columnDate) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(columnDate);
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
                if ("java.sql.Timestamp".equals(name)) {
                    converMap.put(key, ConvertType.TIMESTAMP_TO_STRING);
                    //oracle byte[]
                } else if ("[B".equals(name)) {
                    converMap.put(key, ConvertType.BYTE_TO_BASE64);
                } else if ("org.postgresql.util.PGobject".equals(name)) {
                    converMap.put(key, ConvertType.PGOBJECT_TO_STRING);
                }
                hasCheckColumnList.add(key);
            }
        }
    }

    public static <T> Object convertObject(Class<T> targetType, Object res) {
        if (res==null) {
            return null;
        }
        String sourceTypeName = res.getClass().getName();
        String targetTypename = targetType.getName();
        if ("java.lang.String".equals(targetTypename)) {
            return res.toString();
        }
        System.out.println(targetTypename);
        return res.toString();
    }
}
