package indi.cyh.jdbctool.tool;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName StringTool
 * @Description 字符串工具类, 继承org.apache.commons.lang3.StringUtils类
 * @Author gm
 * @Date 2020/4/11 13:04
 */
public class StringTool {
    /**
     * 判断对象是否为空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(Object str) {
        boolean flag = false;
        if (str != null && !"".equals(str)) {
            if (str.toString().trim().length() > 0) {
                flag = true;
            }
        } else {
            flag = false;
        }
        return flag;
    }

    /**
     * 判断字符串数据是否为空
     *
     * @param strs
     * @return
     */
    public static boolean isEmpty(String[] strs) {
        return !isNotEmpty(strs);
    }

    public static boolean isEmpty(Object strs) {
        return !isNotEmpty(strs);
    }

    /**
     * clob 转string
     *
     * @param clob
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public static String clobToString(Clob clob) throws IOException, SQLException {
        if (clob == null || clob.length() <= 0) {
            return "";
        }
        String reString = "";
        Reader is = clob.getCharacterStream();
        BufferedReader br = new BufferedReader(is);
        String s = br.readLine();
        StringBuilder sb = new StringBuilder();
        while (s != null) {
            sb.append(s);
            s = br.readLine();
        }
        reString = sb.toString();
        br.close();
        is.close();
        return reString;
    }

    /**
     * 字符串转data
     *
     * @param str
     * @return
     */
    public static Date stringToDate(String str) {
        SimpleDateFormat sdf = null;
        if (isEmpty(str)) {
            return null;
        }
        if (str.length() == 21) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        } else if (str.length() == 19) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else if (str.length() == 16) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        }
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    /**
     * 判断字符串数据是否不为空
     *
     * @param strs
     * @return
     */
    public static boolean isNotEmpty(String[] strs) {
        boolean flag = true;
        if (strs == null || strs.length == 0) {
            return false;
        }
        for (String str : strs) {
            if (!isNotEmpty(str)) {
                return false;
            }
        }
        return flag;
    }

    /**
     * @Author CYH
     * @Description
     * @Return
     * @Exception
     * @Date 2019/10/22 11:30
     */
    public static String getSqlValueStr(List<String> list) {
        list = new ArrayList<>(list);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, "'" + list.get(i) + "'");
        }
        return String.join( ",",list);
    }
}
