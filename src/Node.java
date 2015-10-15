import java.util.ArrayList;

public class Node {
  public static class DataType {
    public static final int INT = 1;
    public static final int CHAR = 2;
    public static final int DATE = 3;

    private int type = 0;
    private int length = 0;

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }
    public void print() {
      System.out.println("DataType:");
      System.out.print("type: ");
      System.out.println(type);
      System.out.print("length: ");
      System.out.println(length);
    }
  }

  public static class ColumnDefinition {
    public String columnName = null;
    public DataType dataType = null;
    public boolean notNull = false;

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }
    public boolean isNotNull() { return notNull; }
    public void setNotNull(boolean notNull) { this.notNull = notNull; }
    public void print() {
      System.out.println("ColumnDefinition:");
      System.out.print("columnName: ");
      System.out.println(columnName);
      dataType.print();
      System.out.print("notNull: ");
      System.out.println(notNull);
    }
  }

  public static class PrimaryKeyConstraint {
    private ColumnNameList key;

    public ColumnNameList getKey() { return key; }
    public void setKey(ColumnNameList key) { this.key = key; }
    public void print() {
      System.out.println("PrimaryKeyConstraint:");
      key.print();
    }
  }

  public static class ReferentialConstraint {
    private ColumnNameList referencing;
    private String tableName;
    private ColumnNameList referenced;
    public ColumnNameList getRefrencing() { return referencing; }
    public void setReferencing(ColumnNameList referencing) { this.referencing = referencing; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public ColumnNameList getRefrenced() { return referenced; }
    public void setReferenced(ColumnNameList referenced) { this.referenced = referenced; }
    public void print() {
      System.out.println("ReferentialConstraint:");
      referencing.print();
      System.out.print("tableName: ");
      System.out.println(tableName);
      referenced.print();
    }
  }

  public static class ColumnNameList {
    private ArrayList<String> columnNames = new ArrayList<String>();

    public void addColumnName(String columnName) { columnNames.add(columnName); }
    public void print() {
      System.out.print("ColumnNameList:");
      for(String name : columnNames) {
        System.out.print(" ");
        System.out.print(name);
      }
      System.out.println();
    }
  }
}
