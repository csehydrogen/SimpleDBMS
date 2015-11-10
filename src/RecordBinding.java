import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RecordBinding extends TupleBinding<List<Value>> {
  // encode column object into bytes
  public void objectToEntry(List<Value> lv, TupleOutput to) {
    for (Value v : lv) {
      to.writeInt(v.getType());
      switch(v.getType()) {
        case Value.INT:
          to.writeInt(v.getValInt());
          break;
        case Value.CHAR: case Value.DATE:
          to.writeString(v.getValStr());
          break;
      }
    }
    to.writeInt(-1);
  }

  // decode column object from bytes
  public List<Value> entryToObject(TupleInput ti) {
    List<Value> lv = new ArrayList<Value>();
    while (true) {
      int type = ti.readInt();
      if (type == -1) break;
      Value v = new Value();
      v.setType(type);
      switch (type) {
        case Value.INT:
          v.setValInt(ti.readInt());
          break;
        case Value.CHAR: case Value.DATE:
          v.setValStr(ti.readString());
          break;
      }
      lv.add(v);
    }
    return lv;
  }
}
