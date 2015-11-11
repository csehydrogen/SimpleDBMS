import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {
  // schema manager which manages this table
  private SchemaManager sm;
  // table name
  private String name;
  // column name to column
  private HashMap<String, Column> columns = new HashMap<String, Column>();
  private boolean hasPrimary = false;
  private int numOfCols = 0;

  public int getNumOfCols() { return numOfCols; }
  public Column getColumn(String name) { return columns.get(name); }
  public SchemaManager getSM() { return sm; }

  public Table(SchemaManager sm, String name) {
    this.sm = sm;
    this.name = name;
  }

  public String getName() { return name; }

  public void addColumn(Column column) throws ParseException {
    if(columns.containsKey(column.getName())) {
      Message.print(Message.DUPLICATE_COLUMN_DEF_ERROR);
      throw new ParseException();
    }
    ++numOfCols;
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
    for(Column col : getSortedColumns()) {
      System.out.println(String.format(fmt,
        col.getName(),
        col.getTypeString(),
        col.getNotNullString(),
        col.getKeyString()
      ));
    }
  }

  public List<Column> getSortedColumns() {
    List<Column> cl = new ArrayList<Column>(columns.values());
    Collections.sort(cl);
    return cl;
  }

  /**
   * @param cnl columnNameList
   * @param vl valueList
   */
  public void insert(List<String> cnl, ArrayList<Value> vl) throws ParseException {
    List<Column> cl = getSortedColumns();

    // if <COLUMN NAME LIST> omitted
    if (cnl == null) {
      cnl = new ArrayList<String>();
      for (Column c : cl) {
        cnl.add(c.getName());
      }
      if (cnl.size() > vl.size())
        cnl = cnl.subList(0, vl.size());
    }

    // # of column names != # of values
    if (cnl.size() != vl.size()) {
      Message.print(Message.INSERT_TYPE_MISMATCH_ERROR);
      throw new ParseException();
    }

    // make map from column name to value for efficient search
    Map<String, Value> cn2v = new HashMap<String, Value>();
    for (int i = 0; i < cnl.size(); ++i) {
      String cn = cnl.get(i);
      // if column name doesn't exist
      if(!columns.containsKey(cn)) {
        Message.print(Message.INSERT_COLUMN_EXISTENCE_ERROR, cn);
        throw new ParseException();
      }
      cn2v.put(cnl.get(i), vl.get(i));
    }

    // create record
    vl = new ArrayList<Value>();
    for (Column c : cl) {
      String cn = c.getName();
      Value v;
      if (cn2v.containsKey(cn)) { // value given explicitly
        v = cn2v.get(cn);
        if (v.getType() == Value.NULL) {
          // try to insert null into non-nullable column
          if (c.isNotNull()) {
            Message.print(Message.INSERT_COLUMN_NON_NULLABLE_ERROR, cn);
            throw new ParseException();
          }
        } else {
          // type mismatch
          if (v.getType() != c.getType()) {
            Message.print(Message.INSERT_TYPE_MISMATCH_ERROR);
            throw new ParseException();
          }
          // char truncate
          if (v.getType() == Value.CHAR && v.getValStr().length() > c.getLength()) {
            v.setValStr(v.getValStr().substring(0, c.getLength()));
          }
        }
      } else { // value not given
        if (c.isNotNull()) {
          Message.print(Message.INSERT_COLUMN_NON_NULLABLE_ERROR, cn);
          throw new ParseException();
        }
        v = new Value();
        v.setType(Value.NULL);
      }
      vl.add(v);
    }

    Records here = sm.getRecords(name);
    if(!here.checkPrimary(vl)) {
      Message.print(Message.INSERT_DUPLICATE_PRIMARY_KEY_ERROR);
      throw new ParseException();
    }
    for (int i = 0; i < cl.size(); ++i) {
      Column c = cl.get(i);
      if(c.isForeign() && vl.get(i).getType() != Value.NULL) {
        Foreign f = c.getReferencing();
        Records there = sm.getRecords(f.getTableName());
        if(!there.checkForeign(f.getColumnName(), vl.get(i))) {
          Message.print(Message.INSERT_REFERENTIAL_INTEGRITY_ERROR);
          throw new ParseException();
        }
      }
    }

    sm.insertRecord(name, vl);
  }

  public void delete(ExpTree.WhereExpression we) throws ParseException {
    List<Foreign> lf = new ArrayList<Foreign>();
    we.getForeigns(lf);

    for (Foreign f : lf) {
      String tn = f.getTableName();
      if (tn != null && !tn.equals(name)) {
        Message.print(Message.WHERE_TABLE_NOT_SPECIFIED);
        throw new ParseException();
      }
      if (!columns.containsKey(f.getColumnName())) {
        Message.print(Message.WHERE_COLUMN_NOT_EXIST);
        throw new ParseException();
      }
      f.setCoord(new SelectHelper.Coord(0, columns.get(f.getColumnName()).getOrd()));
    }

    List<Integer> idx = new ArrayList<Integer>();
    Records r = sm.getRecords(name);
    for (int i = 0; i < r.size(); ++i) {
      List<List<Value>> record = new ArrayList<List<Value>>();
      record.add(r.getRecord(i));
      if(we.eval(record).isTrue()) {
        idx.add(i);
      }
    }

    List<Records> cache = new ArrayList<Records>();
    for (Column c : columns.values()) {
      for (Foreign f : c.getReferenced()) {
        cache.add(sm.getRecords(f.getTableName()));
      }
    }

    int cntS = 0, cntF = 0;
    for (int i : idx) {
      int j = 0; boolean flag = true;
      for (Column c : columns.values()) {
        for (Foreign f : c.getReferenced()) {
          Records records = cache.get(j++);
          Column there = records.getTable().getColumn(f.getColumnName());
          if (there.isNotNull() && records.checkForeign(there.getName(), r.getRecord(i).get(c.getOrd()))) {
            flag = false;
          }
        }
      }
      if (flag) {
        ++cntS;
        j = 0;
        for (Column c : columns.values()) {
          for (Foreign f : c.getReferenced()) {
            Records records = cache.get(j++);
            records.remove(f.getColumnName(), r.getRecord(i).get(c.getOrd()));
          }
        }
        sm.deleteRecord(name, r.getRecord(i));
      } else {
        ++cntF;
      }
    }

    Message.print(Message.DELETE_RESULT, Integer.toString(cntS));
    if (cntF > 0)
      Message.print(Message.DELETE_REFERENTIAL_INTEGRITY_PASSED, Integer.toString(cntF));
  }
}
