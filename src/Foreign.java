import java.util.List;

public class Foreign implements Comparable<Foreign>, ExpTree.CompOperand {
  private String tableName;
  private String columnName;
  private SelectHelper.Coord coord;

  public Foreign() {}
  public Foreign(String tableName, String columnName) {
    this.tableName = tableName;
    this.columnName = columnName;
  }

  public String getTableName() { return tableName; }
  public void setTableName(String tableName) { this.tableName = tableName; }
  public String getColumnName() { return columnName; }
  public void setColumnName(String columnName) { this.columnName = columnName; }
  public SelectHelper.Coord getCoord() { return coord; }
  public void setCoord(SelectHelper.Coord coord) { this.coord = coord; }

  public int compareTo(Foreign that) {
    int t = tableName.compareTo(that.tableName);
    return t == 0 ? columnName.compareTo(that.columnName) : t;
  }

  public boolean equals(Foreign that) {
    return tableName.compareTo(that.tableName) == 0 && columnName.compareTo(that.columnName) == 0;
  }

  public void getForeigns(List<Foreign> lf) {
    lf.add(this);
  }

  public Value getValue(List<List<Value>> record) {
    return record.get(coord.getX()).get(coord.getY());
  }
}
