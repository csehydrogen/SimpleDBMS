import java.util.TreeSet;

public class Column {
  public static final int INT = 1;
  public static final int CHAR = 2;
  public static final int DATE = 3;

  private String name;
  private int type;
  private int length = 0;
  private boolean notNull = false;
  private boolean primary = false;
  private boolean foreign = false;
  private Foreign referencing = null;
  private TreeSet<Foreign> referenced = new TreeSet<Foreign>();

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public int getType() { return type; }
  public void setType(int type) { this.type = type; }
  public int getLength() { return length; }
  public void setLength(int length) { this.length = length; }
  public boolean isNotNull() { return notNull; }
  public void setNotNull(boolean notNull) { this.notNull = notNull; }
  public boolean isPrimary() { return primary; }
  public void setPrimary(boolean primary) { this.primary = primary; }
  public boolean isForeign() { return foreign; }
  public void setForeign(boolean foreign) { this.foreign = foreign; }
  public Foreign getReferencing() { return referencing; }
  public void setReferencing(Foreign referencing) { this.referencing = referencing; }
  public TreeSet<Foreign> getReferenced() { return referenced; }

  public String getTypeString() {
    switch(type) {
      case INT: return "int";
      case CHAR: return String.format("char(%d)", length);
      case DATE: return "date";
      default: return "";
    }
  }

  public String getNotNullString() {
    return notNull ? "N" : "Y";
  }

  public String getKeyString() {
    return String.format("%s%s%s",
      primary ? "PRI" : "",
      primary && foreign ? "/" : "",
      foreign ? "FOR" : ""
    );
  }
}
