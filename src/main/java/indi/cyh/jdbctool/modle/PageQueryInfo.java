package indi.cyh.jdbctool.modle;

/**
 * @author CYH
 * @date 2020/3/31 0031 9:44
 */
public class PageQueryInfo {
    private String countSql;
    private String pageSql;

    public PageQueryInfo() {
    }

    public String getCountSql() {
        return this.countSql;
    }

    public void setCountSql(String countSql) {
        this.countSql = countSql;
    }

    public String getPageSql() {
        return this.pageSql;
    }

    public void setPageSql(String pageSql) {
        this.pageSql = pageSql;
    }
}
