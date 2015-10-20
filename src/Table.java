import java.util.ArrayList;
import java.util.HashMap;

public class Table {
  // schema manager which manages this table
  private SchemaManager sm;
  // table name
  private String name;
  // column name to column
  private HashMap<String, Column> columns = new HashMap<String, Column>();
  private boolean hasPrimary = false;

  public Table(SchemaManager sm, String name) {
    this.sm = sm;
    this.name = name;
  }

  public void addColumn(Column column) throws ParseException {
    if(columns.containsKey(column.getName())) {
      Message.print(Message.DUPLICATE_COLUMN_DEF_ERROR);
      throw new ParseException();
    }
    columns.put(column.getName(), column);
  }

  public void addPrimaryKey(ArrayList<String> keys) throws ParseException {
    if(hasPrimary) {
      Message.print(Message.DUPLICATE_PRIMARY_KEY_DEF_ERROR);
      throw new ParseException();
    }
    hasPrimary = true;
    for(String key : keys) {
      if(!columns.containsKey(key)) {
        Message.print(Message.NON_EXISTING_COLUMN_DEF_ERROR, key);
        throw new ParseException();
      }
      Column col = columns.get(key);
      if(col.isPrimary()){
        Message.print(Message.DUPLICATE_PRIMARY_KEY_DEF_ERROR);
        throw new ParseException();
      }
      col.setNotNull(true);
      col.setPrimary(true);
    }
  }

  public void addForeignKey(ArrayList<String> referencing, String tableName, ArrayList<String> referenced) throws ParseException {
    if(referencing.size() != referenced.size()) {
      Message.print(Message.REFERENCE_TYPE_ERROR);
      throw new ParseException();
    }
    if(!sm.tableExists(tableName)) {
      Message.print(Message.REFERENCE_TABLE_EXISTENCE_ERROR);
      throw new ParseException();
    }
    int n = referencing.size();
    for(int i = 0; i < n; ++i) {
      if(!columns.containsKey(referencing.get(i))){
        Message.print(Message.NON_EXISTING_COLUMN_DEF_ERROR, referencing.get(i));
        throw new ParseException();
      }
      Column col_ing = columns.get(referencing.get(i));
      Column col_ed = sm.getColumn(tableName, referenced.get(i));
      if(col_ed == null) {
        Message.print(Message.REFERENCE_COLUMN_EXISTENCE_ERROR);
        throw new ParseException();
      }
      if(col_ing.getType() != col_ed.getType()
        || col_ing.getLength() != col_ed.getLength()) {
        Message.print(Message.REFERENCE_TYPE_ERROR);
        throw new ParseException();
      }
      if(!col_ed.isPrimary()) {
        Message.print(Message.REFERENCE_NON_PRIMARY_KEY_ERROR);
        throw new ParseException();
      }
      if(col_ing.isForeign()) {
        Message.print(Message.DUPLICATE_FOREIGN_KEY_DEF_ERROR);
        throw new ParseException();
      }
      col_ing.setForeign(true);
      col_ing.setReferencing(new Foreign(tableName, col_ed.getName()));
    }
  }

  // check if any columns are referenced by
  public boolean isReferenced() {
    for(Column col : columns.values())
      if(col.getReferenced().size() > 0)
        return true;
    return false;
  }

  // save table to db using schema manager
  public void save() {
    for(Column col : columns.values()) {
      sm.putColumn(name, col);
      if(col.isForeign()) {
        sm.addForeign(
          col.getReferencing().getTableName(),
          col.getReferencing().getColumnName(),
          new Foreign(name, col.getName())
        );
      }
    }
  }

  // for desc statement
  public void print() {
    String fmt = "%-22.22s%-11.11s%-9.9s%-9.9s";
    System.out.println(String.format("table_name [%s]", name));
    System.out.println(String.format(fmt, "column_name", "type", "null", "key"));
    for(Column col : columns.values()) {
      System.out.println(String.format(fmt,
        col.getName(),
        col.getTypeString(),
        col.getNotNullString(),
        col.getKeyString()
      ));
    }
  }
}
