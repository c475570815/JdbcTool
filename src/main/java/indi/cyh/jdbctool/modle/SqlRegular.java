package indi.cyh.jdbctool.modle;


import java.util.regex.Pattern;

public class SqlRegular {
    //sql 匹配相应参数
    public static final Pattern selectPattern;
    public static final Pattern fromPattern;
    public static final Pattern PATTERN_BRACKET;
    public static final Pattern PATTERN_SELECT;
    public static final Pattern PATTERN_DISTINCT;
    public static final Pattern rxOrderBy;

    static {
        //sql 匹配相应参数 赋值
        selectPattern = Pattern.compile("\\s*(SELECT|EXECUTE|CALL)\\s", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.MULTILINE | Pattern.UNICODE_CASE);
        fromPattern = Pattern.compile("\\s*FROM\\s", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE);
        PATTERN_BRACKET = Pattern.compile("(\\(|\\)|[^\\(\\)]*)");
        PATTERN_SELECT = Pattern.compile("select([\\W\\w]*)from", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.MULTILINE | Pattern.UNICODE_CASE);
        PATTERN_DISTINCT = Pattern.compile("\\A\\s+DISTINCT\\s", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.MULTILINE | Pattern.UNICODE_CASE);
        rxOrderBy = Pattern.compile("\\bORDER\\s+BY\\s+([\\W\\w]*)(ASC|DESC)+", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.MULTILINE | Pattern.UNICODE_CASE);
    }
}
