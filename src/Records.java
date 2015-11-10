import java.util.ArrayList;
import java.util.List;

public class Records {
  private Table table;
  private List<List<Value>> records = new ArrayList<List<Value>>();

  public Records(Table t) {
    table = t;
  }

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
}
