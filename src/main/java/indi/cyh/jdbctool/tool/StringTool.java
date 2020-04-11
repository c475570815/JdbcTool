package indi.cyh.jdbctool.tool;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.util.Strings.isBlank;

/**
 * @ClassName StringTool
 * @Description 字符串工具类, 继承org.apache.commons.lang3.StringUtils类
 * @Author gm
 * @Date 2020/4/11 13:04
 */
public class StringTool {
    /**
     * 首字母变小写
     */
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     */
    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    public static String replaceEnter(String str) {
        return str.replace("\n", "");
    }


    public static String getUUId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 替换掉HTML标签方法
     */
    public static String replaceHtml(String html) {
        if (isBlank(html)) {
            return "";
        }
        String regEx = "<.+?>";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(html);
        String s = m.replaceAll("");
        return s;
    }

//    /**
//     * 缩略字符串（不区分中英文字符）
//     *
//     * @param str    目标字符串
//     * @param length 截取长度
//     * @return
//     */
//    public static String abbr(String str, int length) {
//        if (str == null) {
//            return "";
//        }
//        try {
//            StringBuilder sb = new StringBuilder();
//            int currentLength = 0;
//            for (char c : replaceHtml(StringEscapeUtils.unescapeHtml4(str)).toCharArray()) {
//                currentLength += String.valueOf(c).getBytes("GBK").length;
//                if (currentLength <= length - 3) {
//                    sb.append(c);
//                } else {
//                    sb.append("...");
//                    break;
//                }
//            }
//            return sb.toString();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }


    /**
     * 自定义的分隔字符串函数 例如: 1,2,3 =>[1,2,3] 3个元素 ,2,3=>[,2,3] 3个元素 ,2,3,=>[,2,3,] 4个元素 ,,,=>[,,,] 4个元素
     * <p>
     * 5.22算法修改，为提高速度不用正则表达式 两个间隔符,,返回""元素
     *
     * @param split 分割字符 默认,
     * @param src   输入字符串
     * @return 分隔后的list
     * @author Robin
     */
    public static List<String> splitToList(String split, String src) {
        // 默认,
        String sp = ",";
        if (split != null && split.length() == 1) {
            sp = split;
        }
        List<String> r = new ArrayList<String>();
        int lastIndex = -1;
        int index = src.indexOf(sp);
        if (-1 == index && src != null) {
            r.add(src);
            return r;
        }
        while (index >= 0) {
            if (index > lastIndex) {
                r.add(src.substring(lastIndex + 1, index));
            } else {
                r.add("");
            }

            lastIndex = index;
            index = src.indexOf(sp, index + 1);
            if (index == -1) {
                r.add(src.substring(lastIndex + 1, src.length()));
            }
        }
        return r;
    }


    public static boolean strPos(String sou, List<String> finds) {
        if (sou != null && finds != null && finds.size() > 0) {
            for (String s : finds) {
                if (sou.indexOf(s) > -1)
                    return true;
            }
        }
        return false;
    }


    /**
     * 判断两个字符串是否相等 如果都为null则判断为相等,一个为null另一个not null则判断不相等 否则如果s1=s2则相等
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }


//    /**
//     * 随即生成指定位数的含验证码字符串
//     *
//     * @param bit 指定生成验证码位数
//     * @return String
//     * @author Peltason
//     * @date 2007-5-9
//     */
//    public static String random(int bit) {
//        if (bit == 0)
//            bit = 6; // 默认6位
//        // 因为o和0,l和1很难区分,所以,去掉大小写的o和l
//        String str = "";
//        str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";// 初始化种子
//        return RandomStringUtils.random(bit, str);// 返回6位的字符串
//    }


    /**
     * 页面中去除字符串中的空格、回车、换行符、制表符
     *
     * @param str
     * @return
     * @author shazao
     * @date 2007-08-17
     */
    public static String replaceBlank(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            str = m.replaceAll("");
        }
        return str;
    }


    /**
     * 泛型方法(通用)，把list转换成以“,”相隔的字符串 调用时注意类型初始化（申明类型） 如：List<Integer> intList = new ArrayList<Integer>(); 调用方法：StringUtils.listTtoString(intList); 效率：list中4条信息，1000000次调用时间为850ms左右
     *
     * @param <T>  泛型
     * @param list list列表
     * @return 以“,”相隔的字符串
     * @author fengliang
     * @serialData 2008-01-09
     */
    public static <T> String listTtoString(List<T> list) {
        if (list == null || list.size() < 1)
            return "";
        Iterator<T> i = list.iterator();
        if (!i.hasNext())
            return "";
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            T e = i.next();
            sb.append(e);
            if (!i.hasNext())
                return sb.toString();
            sb.append(",");
        }
    }


    /**
     * 判断文字内容重复
     *
     * @author 沙枣
     * @Date 2008-04-17
     */
    public static boolean isContentRepeat(String content) {
        int similarNum = 0;
        int forNum = 0;
        int subNum = 0;
        int thousandNum = 0;
        String startStr = "";
        String nextStr = "";
        boolean result = false;
        float endNum = (float) 0.0;
        if (content != null && content.length() > 0) {
            if (content.length() % 1000 > 0)
                thousandNum = (int) Math.floor(content.length() / 1000) + 1;
            else
                thousandNum = (int) Math.floor(content.length() / 1000);
            if (thousandNum < 3)
                subNum = 100 * thousandNum;
            else if (thousandNum < 6)
                subNum = 200 * thousandNum;
            else if (thousandNum < 9)
                subNum = 300 * thousandNum;
            else
                subNum = 3000;
            for (int j = 1; j < subNum; j++) {
                if (content.length() % j > 0)
                    forNum = (int) Math.floor(content.length() / j) + 1;
                else
                    forNum = (int) Math.floor(content.length() / j);
                if (result || j >= content.length())
                    break;
                else {
                    for (int m = 0; m < forNum; m++) {
                        if (m * j > content.length() || (m + 1) * j > content.length() || (m + 2) * j > content.length())
                            break;
                        startStr = content.substring(m * j, (m + 1) * j);
                        nextStr = content.substring((m + 1) * j, (m + 2) * j);
                        if (startStr.equals(nextStr)) {
                            similarNum = similarNum + 1;
                            endNum = (float) similarNum / forNum;
                            if (endNum > 0.4) {
                                result = true;
                                break;
                            }
                        } else
                            similarNum = 0;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 判断是否是空字符串 null和"" null返回result,否则返回字符串
     *
     * @param s
     * @return
     */
    public static String isEmpty(String s, String result) {
        if (s != null && !"".equals(s)) {
            return s;
        }
        return result;
    }

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


    public static String getProperty(String property) {
        if (property.contains("_")) {
            return property.replaceAll("_", "\\.");
        }
        return property;
    }

    /**
     * 解析前台encodeURIComponent编码后的参数
     *
     * @return
     */
    public static String getEncodePra(String property) {
        String trem = "";
        if (isNotEmpty(property)) {
            try {
                trem = URLDecoder.decode(property, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return trem;
    }

    // 判断一个字符串是否都为数字
    public boolean isDigit(String strNum) {
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) strNum);
        return matcher.matches();
    }

    // 截取数字
    public String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    // 截取非数字
    public String splitNotNumber(String content) {
        Pattern pattern = Pattern.compile("\\D+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 判断某个字符串是否存在于数组中
     *
     * @param stringArray 原数组
     * @param source      查找的字符串
     * @return 是否找到
     */
    public static boolean contains(String[] stringArray, String source) {
        // 转换为list
        List<String> tempList = Arrays.asList(stringArray);

        // 利用list的包含方法,进行判断
        if (tempList.contains(source)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 中文日期转换
     *
     * @param num 传yyy或MM或dd
     * @return
     */
    public static String chineseYMD(String num) {
        String[] chinese = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        int slen = num.length();
        String result = "";
        if (slen > 2) {
            for (int i = 0; i < slen; i++) {
                int numInt = Integer.parseInt(num.substring(i, i + 1));
                result += chinese[numInt];
            }
        } else {
            int numInt = Integer.parseInt(num);
            if (numInt < 10) {
                result = "零" + chinese[numInt];
            } else {
                for (int i = 0; i < slen; i++) {
                    int num1 = Integer.parseInt(num.substring(i, i + 1));
                    if (i == 0) {
                        result += chinese[num1] + "拾";
                    } else if (num1 != 0) {
                        result += chinese[num1];
                    }
                }
            }
            if (numInt == 10 || numInt == 20 || numInt == 30) {
                result = "零" + result;
            }
        }
        return result;
    }

    /**
     * 数字金额转换
     *
     * @param num 金额信息
     * @return
     */
    public static String numToChinese(String num) {
        String[] chinese = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        int slen = num.length();
        String result = "";
        for (int i = 0; i < slen; i++) {
            int numInt = Integer.parseInt(num.substring(i, i + 1));
            result += chinese[numInt];
        }
        return result;
    }

    public static boolean isNoneBlank(CharSequence... css) {
        return !isAnyBlank(css);
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

    // 把null格式化成""
    public static String formatNull(String str) {
        if (str == null || "null".equals(str))
            return "";
        else
            return str;
    }


    public static boolean isAllBlank(String... strings) {
        String[] arr$ = strings;
        int len$ = strings.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            String string = arr$[i$];
            if (!isBlank(string)) {
                return false;
            }
        }

        return true;
    }
}
