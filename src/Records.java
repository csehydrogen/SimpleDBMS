import java.util.ArrayList;
import java.util.List;

public class Records {
  private Table table;
  private List<List<Value>> records = new ArrayList<List<Value>>();

  public Records(Table t) {
    table = t;
  }

  public Table getTable() { return table; }
  public List<Value> getRecord(int i) { return records.get(i); }
  public int size() { return records.size(); }

  public void addRecord(List<Value> record) {
    records.add(record);
  }

  public boolean checkPrimary(List<Value> cand) {
    List<Column> columns = table.getSortedColumns();
    int csz = columns.size();
    List<Integer> idx = new ArrayList<Integer>();
    for (int i = 0; i < csz; ++i) {
      if (columns.get(i).isPrimary()) {
        idx.add(i);
      }
    }
    if(idx.size() == 0) return true;
    for (List<Value> record : records) {
      boolean flag = true;
      for (int i : idx) {
        if(columns.get(i).isPrimary() && !record.get(i).equals(cand.get(i))) {
          flag = false;
          break;
        }
      }
      if (flag)
        return false;
    }
    return true;
  }

  public boolean checkForeign(String colName, Value v) {
    int i = table.getColumn(colName).getOrd();
    for (List<Value> record : records) {
      if (record.get(i).equals(v)) {
        return true;
      }
    }
    return false;
  }

  public void remove(String colName, Value v) {
    int i = table.getColumn(colName).getOrd();
    for (int j = 0; j < records.size(); ++j) {
      List<Value> record = records.get(j);
      if (record.get(i).equals(v)) {
        List<Value> newRecord = new ArrayList<Value>(record);
        Value newValue = new Value();
        newValue.setType(Value.NULL);
        newRecord.set(i, newValue);
        table.getSM().updateRecord(table.getName(), record, newRecord);
        records.set(j, newRecord);
      }
    }
  }
}
