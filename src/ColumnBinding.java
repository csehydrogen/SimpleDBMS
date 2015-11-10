import java.util.TreeSet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ColumnBinding extends TupleBinding<Column> {
  // encode column object into bytes
  public void objectToEntry(Column col, TupleOutput to) {
    to.writeString(col.getName());
    to.writeInt(col.getOrd());
    to.writeInt(col.getType());
    to.writeInt(col.getLength());
    to.writeBoolean(col.isNotNull());
    to.writeBoolean(col.isPrimary());
    to.writeBoolean(col.isForeign());

    if(col.isForeign()) {
      Foreign referencing = col.getReferencing();
      to.writeString(referencing.getTableName());
      to.writeString(referencing.getColumnName());
    }

    TreeSet<Foreign> referenced = col.getReferenced();
    to.writeInt(referenced.size());
    for(Foreign f : referenced) {
      to.writeString(f.getTableName());
      to.writeString(f.getColumnName());
    }
  }

  // decode column object from bytes
  public Column entryToObject(TupleInput ti) {
    int n;
    Column col = new Column();

    col.setName(ti.readString());
    col.setOrd(ti.readInt());
    col.setType(ti.readInt());
    col.setLength(ti.readInt());
    col.setNotNull(ti.readBoolean());
    col.setPrimary(ti.readBoolean());
    col.setForeign(ti.readBoolean());

    if(col.isForeign()) {
      Foreign referencing = new Foreign();
      referencing.setTableName(ti.readString());
      referencing.setColumnName(ti.readString());
      col.setReferencing(referencing);
    }

    n = ti.readInt();
    TreeSet<Foreign> referenced = col.getReferenced();
    for(int i = 0; i < n; ++i) {
      Foreign f = new Foreign();
      f.setTableName(ti.readString());
      f.setColumnName(ti.readString());
      referenced.add(f);
    }

    return col;
  }
}
