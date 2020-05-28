package indi.cyh.jdbctool.entity;


import indi.cyh.jdbctool.toolinterface.FieldColumn;
import indi.cyh.jdbctool.toolinterface.PrimaryField;
import indi.cyh.jdbctool.toolinterface.TableName;

@TableName("bs_diary")
@PrimaryField("d_diaryId")
public class BsDiary {
  @FieldColumn("d_diaryId")
  private long dDiaryId;
  @FieldColumn("d_head")
  private String dHead;
  @FieldColumn("d_content")
  private String dContent;
  @FieldColumn("d_type")
  private String dType;
  @FieldColumn("d_date")
  private java.sql.Date dDate;
  @FieldColumn("d_time")
  private java.sql.Timestamp dTime;


  public long getDDiaryId() {
    return dDiaryId;
  }

  public void setDDiaryId(long dDiaryId) {
    this.dDiaryId = dDiaryId;
  }


  public String getDHead() {
    return dHead;
  }

  public void setDHead(String dHead) {
    this.dHead = dHead;
  }


  public String getDContent() {
    return dContent;
  }

  public void setDContent(String dContent) {
    this.dContent = dContent;
  }


  public String getDType() {
    return dType;
  }

  public void setDType(String dType) {
    this.dType = dType;
  }


  public java.sql.Date getDDate() {
    return dDate;
  }

  public void setDDate(java.sql.Date dDate) {
    this.dDate = dDate;
  }


  public java.sql.Timestamp getDTime() {
    return dTime;
  }

  public void setDTime(java.sql.Timestamp dTime) {
    this.dTime = dTime;
  }

}
