public class Foreign implements Comparable<Foreign>{
  private String tableName;
  private String columnName;

  public Foreign() {}
  public Foreign(String tableName, String columnName) {
    this.tableName = tableName;
    this.columnName = columnName;
  }

  public String getTableName() { return tableName; }
  public void setTableName(String tableName) { this.tableName = tableName; }
  public String getColumnName() { return columnName; }
  public void setColumnName(String columnName) { this.columnName = columnName; }

  public int compareTo(Foreign that) {
    int t = tableName.compareTo(that.tableName);
    return t == 0 ? columnName.compareTo(that.columnName) : t;
  }
}
