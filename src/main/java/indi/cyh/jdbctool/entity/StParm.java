package indi.cyh.jdbctool.entity;


import indi.cyh.jdbctool.toolinterface.FieldColumn;
import indi.cyh.jdbctool.toolinterface.PrimaryField;
import indi.cyh.jdbctool.toolinterface.TableName;

import java.io.Serializable;

/**
 * @author CYH
 */
@TableName("ST_PARM")
@PrimaryField("PARM_ID")
public class StParm implements Serializable {

    /**
     * 参数编号
     */
    @FieldColumn("PARM_ID")
    private String parmId;
    /**
     * 参数分类
     */
    @FieldColumn("CATEGORY")
    private String category;
    /**
     * 参数名称
     */
    @FieldColumn("NAME")
    private String name;
    /**
     * 参数默认值
     */
    @FieldColumn("DEFAULT_VALUE")
    private String defaultValue;
    /**
     * 参数当前值
     */
    @FieldColumn("NOW_VALUE")
    private String nowValue;
    /**
     * 参数运行值
     */
    @FieldColumn("RUN_VALUE")
    private String runValue;
    /**
     * 参数最小值
     */
    @FieldColumn("MIN_VALUE")
    private String minValue;
    /**
     * 参数最大值
     */
    @FieldColumn("MAX_VALUE")
    private String maxValue;
    /**
     * 参数格式
     */
    @FieldColumn("FORMAT")
    private String format;
    /**
     * 备注
     */
    @FieldColumn("NOTE")
    private String note;
    /**
     * 使用标志
     */
    @FieldColumn("STATE")
    private String state;
    /**
     * 排序
     */
    @FieldColumn("IDX")
    private String idx;
    /**
     * 系统编号
     */
    @FieldColumn("MIS_ID")
    private String misId;


    public String getParmId() {
        return parmId;
    }

    public void setParmId(String parmId) {
        this.parmId = parmId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getNowValue() {
        return nowValue;
    }

    public void setNowValue(String nowValue) {
        this.nowValue = nowValue;
    }

    public String getRunValue() {
        return runValue;
    }

    public void setRunValue(String runValue) {
        this.runValue = runValue;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getMisId() {
        return misId;
    }

    public void setMisId(String misId) {
        this.misId = misId;
    }
}
