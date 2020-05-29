package indi.cyh.jdbctool.tool;

import com.sun.deploy.util.StringUtils;

import java.util.ArrayList;
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

    public static boolean isAnyBlank(CharSequence... css) {
        if (css == null || css.length == 0) {
            return true;
        } else {
            CharSequence[] arr$ = css;
            int len$ = css.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                CharSequence cs = arr$[i$];
                if (cs == null) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * @Author CYH
     * @Description
     * @Return
     * @Exception
     * @Date 2019/10/22 11:30
     */
    public static String getSqlValueStr(String[] arr) {
        List<String> strArr = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            strArr.add("'" + arr[i].toString() + "'");
        }
        return StringUtils.join(strArr, ",");
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
        return StringUtils.join(list, ",");
    }
    /**
     * @Author CYH
     * @Description
     * @Return
     * @Exception
     * @Date 2019/10/22 11:30
     */
    public  static String getSqlColumnStr(List<String> list) {
        list = new ArrayList<>(list);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, "\"" + list.get(i) + "\"");
        }
        return StringUtils.join(list, ",");
    }
}
