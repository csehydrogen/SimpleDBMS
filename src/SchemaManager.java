import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;

public class SchemaManager {
  private Environment env;
  // table name to column
  private Database db;
  private Database records;
  private DatabaseConfig dbConfig;
  private ColumnBinding cb = new ColumnBinding();
  private RecordBinding rb = new RecordBinding();

  public SchemaManager() {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    env = new Environment(new File("db"), envConfig);

    dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setSortedDuplicates(true);
    db = env.openDatabase(null, "db", dbConfig);
    records = env.openDatabase(null, "records", dbConfig);
  }

  public void close() {
    records.close();
    db.close();
    env.close();
  }

  // check if table with given table name exists
  public boolean tableExists(String tableName) {
    try {
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      data.setPartial(0, 0, true);
      return db.get(null, key, data, null) == OperationStatus.SUCCESS;
    } catch(Exception e) {
      return false;
    }
  }

  // drop all tables
  public void dropTables() {
    records.close();
    env.truncateDatabase(null, "records", false);
    records = env.openDatabase(null, "records", dbConfig);

    db.close();
    env.truncateDatabase(null, "db", false);
    db = env.openDatabase(null, "db", dbConfig);
  }

  // drop table with given table name
  public void dropTable(String tableName) {
    Cursor cur = null;
    try {
      cur = db.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      cur.getSearchKey(key, data, null);
      do {
        cur.delete();
        Column col = cb.entryToObject(data);
        if(col.isForeign()) {
          removeForeign(
            col.getReferencing().getTableName(),
            col.getReferencing().getColumnName(),
            new Foreign(tableName, col.getName())
          );
        }
      } while(cur.getNextDup(key, data, null) == OperationStatus.SUCCESS);
    } catch(Exception e) {
    }
    if(cur != null) cur.close();

    try {
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      records.delete(null, key);
    } catch (Exception e) {
    }
  }

  // get string list of all table name
  public ArrayList<String> getTables() {
    ArrayList<String> l = new ArrayList<String>();
    Cursor cur = null;
    try {
      cur = db.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry data = new DatabaseEntry();
      data.setPartial(0, 0, true);
      if(cur.getFirst(key, data, null) == OperationStatus.SUCCESS) {
        do {
          l.add(new String(key.getData(), "UTF-8"));
        } while(cur.getNextNoDup(key, data, null) == OperationStatus.SUCCESS);
      }
    } catch(Exception e) {
    }
    if(cur != null) cur.close();
    return l;
  }

  // get table with given table name
  public Table getTable(String tableName) {
    Table t = new Table(this, tableName);
    Cursor cur = null;
    try {
      cur = db.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      cur.getSearchKey(key, data, null);
      do {
        t.addColumn(cb.entryToObject(data));
      } while(cur.getNextDup(key, data, null) == OperationStatus.SUCCESS);
    } catch(Exception e) {
    }
    if(cur != null) cur.close();
    return t;
  }

  // remove foreign key referenced-by info from specified column
  public void removeForeign(String tableName, String colName, Foreign f) {
    Cursor cursor = null;
    try {
      cursor = db.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry(colName.getBytes("UTF-8"));
      if(cursor.getSearchBothRange(key, data, null) == OperationStatus.SUCCESS) {
        Column col = cb.entryToObject(data);
        if(col.getName().compareTo(colName) == 0) {
          cursor.delete();
          col.getReferenced().remove(f);
          cb.objectToEntry(col, data);
          db.put(null, key, data);
        }
      }
    } catch(Exception e) {
    }
    if(cursor != null)
      cursor.close();
  }

  // add foreign key referenced-by info to specified column
  public void addForeign(String tableName, String colName, Foreign f) {
    Cursor cursor = null;
    try {
      cursor = db.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry(colName.getBytes("UTF-8"));
      if(cursor.getSearchBothRange(key, data, null) == OperationStatus.SUCCESS) {
        Column col = cb.entryToObject(data);
        if(col.getName().compareTo(colName) == 0) {
          cursor.delete();
          col.getReferenced().add(f);
          cb.objectToEntry(col, data);
          db.put(null, key, data);
        }
      }
    } catch(Exception e) {
    }
    if(cursor != null)
      cursor.close();
  }

  public Column getColumn(String tableName, String colName) {
    Cursor cursor = null;
    Column ret = null;
    try {
      cursor = db.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry(colName.getBytes("UTF-8"));
      if(cursor.getSearchBothRange(key, data, null) == OperationStatus.SUCCESS) {
        Column col = cb.entryToObject(data);
        // because ranged search is used, we need to check exact column name
        if(col.getName().compareTo(colName) == 0)
          ret = col;
      }
    } catch(Exception e) {
    }
    if(cursor != null)
      cursor.close();
    return ret;
  }

  public void putColumn(String tableName, Column col) {
    try {
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      cb.objectToEntry(col, data);
      db.put(null, key, data);
    } catch(Exception e) {
    }
  }

  public void insertRecord(String tableName, List<Value> lv) {
    try {
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      rb.objectToEntry(lv, data);
      records.put(null, key, data);
    } catch (Exception e) {
    }
  }

  public void updateRecord(String tableName, List<Value> ob, List<Value> nb) {
    Cursor cur = null;
    try {
      cur = records.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      rb.objectToEntry(ob, data);
      cur.getSearchBoth(key, data, null);
      cur.delete();
      rb.objectToEntry(nb, data);
      records.put(null, key, data);
    } catch(Exception e) {
      e.printStackTrace();
    }
    if(cur != null) cur.close();
  }

  public void deleteRecord(String tableName, List<Value> lv) {
    Cursor cur = null;
    try {
      cur = records.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      rb.objectToEntry(lv, data);
      cur.getSearchBoth(key, data, null);
      cur.delete();
    } catch(Exception e) {
    }
    if(cur != null) cur.close();
  }

  public Records getRecords(String tableName) {
    Records r = new Records(getTable(tableName));
    Cursor cur = null;
    try {
      cur = records.openCursor(null, null);
      DatabaseEntry key = new DatabaseEntry(tableName.getBytes("UTF-8"));
      DatabaseEntry data = new DatabaseEntry();
      cur.getSearchKey(key, data, null);
      do {
        r.addRecord(rb.entryToObject(data));
      } while(cur.getNextDup(key, data, null) == OperationStatus.SUCCESS);
    } catch(Exception e) {
    }
    if(cur != null) cur.close();
    return r;
  }
}
