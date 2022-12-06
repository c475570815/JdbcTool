package indi.cyh.sailjdbc.core;

import indi.cyh.sailjdbc.modle.DBType;
import indi.cyh.sailjdbc.modle.SqlRegular;

import java.util.regex.Matcher;

/**
 * @ClassName SqlMaker
 * @Description TODO
 * @Author CYH
 * @Date 2022/11/29 10:47
 */
public class SqlHandler {
    /**
     * 获取select的列
     *
     * @param sql
     * @return java.lang.String
     * @author CYH
     * @date 2022/11/29 10:52
     **/
    public static String getSelectCols(String sql) {
        Matcher matcherSelect = SqlRegular.PATTERN_SELECT.matcher(sql);
        if (!matcherSelect.find()) {
            throw new RuntimeException("未从sql种匹配到select from ");
        } else {
            return matcherSelect.group(1);
        }
    }

    /**
     * 获取查询总数sql
     *
     * @param sql
     * @return java.lang.String
     * @author CYH
     * @date 2022/11/29 10:52
     **/
    public static String getSelectCountSql(String sql, DBType dbType) throws Exception {
        if (dbType == DBType.SQLSERVER) {
            String formatSql = getFormatSql(sql);
            return String.format("select COUNT(1) from (select  top 100 percent %s) pageTable", formatSql);
        } else {
            return String.format("select COUNT(1) from (%s) pageTable", sql);
        }
    }

    /**
     * 获取分页数据查询sql
     *
     * @param page
     * @param rows
     * @param sql
     * @param dbType
     * @return java.lang.String
     * @author CYH
     * @date 2022/11/29 10:54
     **/
    public static String getPageSql(Integer page, Integer rows, String sql, DBType dbType) {
        String res;
        switch (dbType) {
            case MYSQL:
                res = getMySqlPageSql(page, rows, sql);
                break;
            case SQLSERVER:
                res = getSqlServerPageSql(page, rows, sql);
                break;
            default:
                res = getDefaultPageSql(page, rows, sql);
        }
        return res;
    }

    private static String getSqlServerPageSql(Integer page, Integer rows, String sql) {
        long skip = (long) (page - 1) * rows;
        int lastOrderIndex = sql.toLowerCase().lastIndexOf("order");
        String sqlOrderBy = null;
        if (lastOrderIndex > -1) {
            sqlOrderBy = sql.substring(lastOrderIndex);
        }
        String formatSQL = getFormatSql(sql);
        return String.format("SELECT  * FROM (SELECT top 100 percent ROW_NUMBER() OVER (%s) peta_rn, %s) peta_paged WHERE peta_rn>" + skip + " AND peta_rn<=" + (skip + (long) rows) + "", sqlOrderBy == null ? "ORDER BY NULL" : sqlOrderBy, formatSQL);
    }

    private static String getFormatSql(String sql) {
        String res = "";
        int lastOrderIndex = sql.toLowerCase().lastIndexOf("order");
        String sqlOrderBy = null;
        if (lastOrderIndex > -1) {
            sqlOrderBy = sql.substring(lastOrderIndex);
        }
        String sqlSelectCols = getSelectCols(sql);
        int firstSelectIndex = sql.toLowerCase().indexOf("select");
        Matcher matcherDistinct = SqlRegular.PATTERN_DISTINCT.matcher(sqlSelectCols);
        if (!matcherDistinct.find() && !"*".equalsIgnoreCase(sqlSelectCols.trim())) {
            res = sql.substring(firstSelectIndex + 6);
        } else {
            res = " peta_table.* from (" + sql + ") peta_table ";
            sqlOrderBy = sqlOrderBy == null ? null : sqlOrderBy.replaceAll("([A-Za-z0-9_]*)\\.", "peta_table.");
        }
        return res;
    }

    private static String getMySqlPageSql(Integer page, Integer rows, String sql) {
        return String.format("select * from (%s) pageTable limit %s,%s", sql, (page - 1) * rows, rows);
    }

    private static String getDefaultPageSql(Integer page, Integer rows, String sql) {
        String sqlSelectCols = getSelectCols(sql);
        long skip = (long) (page - 1) * rows;
        Matcher matcherDistinct = SqlRegular.PATTERN_DISTINCT.matcher(sqlSelectCols);
        int lastOrderIndex = sql.toLowerCase().lastIndexOf("order");
        String sqlOrderBy = null;
        if (lastOrderIndex > -1) {
            sqlOrderBy = sql.substring(lastOrderIndex);
        }

        int firstSelectIndex = sql.toLowerCase().indexOf("select");
        String formatSQL;
        if (!matcherDistinct.find() && !"*".equalsIgnoreCase(sqlSelectCols.trim())) {
            formatSQL = sql.substring(firstSelectIndex + 6);
        } else {
            formatSQL = " peta_table.* from (" + sql + ") peta_table ";
            sqlOrderBy = sqlOrderBy == null ? null : sqlOrderBy.replaceAll("([A-Za-z0-9_]*)\\.", "peta_table.");
        }
        return String.format("SELECT * FROM (SELECT ROW_NUMBER() OVER (%s) peta_rn, %s) peta_paged WHERE peta_rn>" + skip + " AND peta_rn<=" + (skip + (long) rows) + "", sqlOrderBy == null ? "ORDER BY NULL" : sqlOrderBy, formatSQL);
    }
}
