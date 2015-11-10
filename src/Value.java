public class Value {
  public static final int NULL = 0;
  public static final int INT = 1;
  public static final int CHAR = 2;
  public static final int DATE = 3;

  private int type;
  private int valInt;
  private String valStr;

  public int getType() { return type; }
  public void setType(int type) { this.type = type; }
  public int getValInt() { return valInt; }
  public void setValInt(int valInt) { this.valInt = valInt; }
  public String getValStr() { return valStr; }
  public void setValStr(String valStr) { this.valStr = valStr;}

  public boolean equals(Value that) {
    if (type != that.type) return false;
    switch (type) {
      case INT:
        return valInt == that.valInt;
      case CHAR: case DATE:
        return valStr.compareTo(that.valStr) == 0;
      default:
        return true;
    }
  }
}
