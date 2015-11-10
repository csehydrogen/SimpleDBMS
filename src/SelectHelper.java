import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SelectHelper {
  // select columns
  private List<Foreign> sc = new ArrayList<Foreign>(); // * if empty
  // as
  private List<String> cAlias = new ArrayList<String>(); // nullable
  // from tables
  private List<Records> r2 = new ArrayList<Records>();
  // as
  private List<String> rAlias = new ArrayList<String>(); // nullable
  // where
  private ExpTree.WhereExpression we;

  private List<Coord> cc = new ArrayList<Coord>();

  public static class Coord {
    private int x, y, cnt = 1;
    public Coord(int x, int y) { this.x = x; this.y = y; }
    public void inc() { ++cnt; }
    public int getCnt() { return cnt; }
    public int getX() { return x; }
    public int getY() { return y; }
  }

  public void selectColumn(Foreign f, String alias) {
    sc.add(f);
    cAlias.add(alias);
  }

  public void addRecords(Records r, String alias) {
    r2.add(r);
    rAlias.add(alias);
  }

  public void setWhereExpression(ExpTree.WhereExpression we) { this.we = we; }

  public void process() throws ParseException {
    // mapping based on from clause
    Map<String, Coord> cn2coord = new TreeMap<String, Coord>();
    Map<Foreign, Coord> tcn2coord = new TreeMap<Foreign, Coord>();
    for (int i = 0; i < r2.size(); ++i) {
      Records r = r2.get(i);
      Table t = r.getTable();
      String tn = t.getName();
      List<Column> lc = t.getSortedColumns();
      for (int j = 0; j < lc.size(); ++j) {
        Column c = lc.get(j);
        String cn = c.getName();
        Coord coord = cn2coord.get(cn);
        if (coord == null) {
          cn2coord.put(cn, new Coord(i, j));
        } else {
          coord.inc();
        }
        Foreign f = new Foreign(tn, cn);
        coord = tcn2coord.get(f);
        if (coord == null) {
          tcn2coord.put(f, new Coord(i, j));
        } else {
          coord.inc();
        }
        if (rAlias.get(i) != null) {
          f = new Foreign(rAlias.get(i), cn);
          coord = tcn2coord.get(f);
          if (coord == null) {
            tcn2coord.put(f, new Coord(i, j));
          } else {
            coord.inc();
          }
        }
      }
    }

    // check ambiguity of where clause
    List<Foreign> lf = new ArrayList<Foreign>();
    we.getForeigns(lf);
    for (Foreign f : lf) {
      if (f.getTableName() == null) {
        Coord coord = cn2coord.get(f.getColumnName());
        if (coord == null) {
          Message.print(Message.WHERE_COLUMN_NOT_EXIST);
          throw new ParseException();
        }
        if (coord.getCnt() > 1) {
          Message.print(Message.WHERE_AMBIGUOUS_REFERENCE);
          throw new ParseException();
        }
      } else {
        Coord coord = tcn2coord.get(f);
        if (coord == null) {
          if (cn2coord.containsKey(f.getColumnName())) {
            Message.print(Message.WHERE_TABLE_NOT_SPECIFIED);
            throw new ParseException();
          } else {
            Message.print(Message.WHERE_COLUMN_NOT_EXIST);
            throw new ParseException();
          }
        }
        if (coord.getCnt() > 1) {
          Message.print(Message.WHERE_AMBIGUOUS_REFERENCE);
          throw new ParseException();
        }
      }
    }

    // check ambiguity of select clause
    for (Foreign f : sc) {
      Coord coord;
      if (f.getTableName() == null) {
        coord = cn2coord.get(f.getColumnName());
      } else {
        coord = tcn2coord.get(f);
      }
      if (coord == null || coord.getCnt() > 1) {
        Message.print(Message.SELECT_COLUMN_RESOLVE_ERROR, f.getColumnName());
        throw new ParseException();
      }
    }

    // set coord of where clause
    for (Foreign f : lf) {
      if (f.getTableName() == null) {
        f.setCoord(cn2coord.get(f.getColumnName()));
      } else {
        f.setCoord(tcn2coord.get(f));
      }
    }

    // set coord of select clause
    for (Foreign f : sc) {
      if (f.getTableName() == null) {
        cc.add(cn2coord.get(f.getColumnName()));
      } else {
        cc.add(tcn2coord.get(f));
      }
    }

    List<Integer> idx = new ArrayList<Integer>();
    List<List<Value>> record = new ArrayList<List<Value>>();
    List<List<Integer>> idxes = new ArrayList<List<Integer>>();
    boolean flag = true;
    for (int i = 0; i < r2.size(); ++i) {
      idx.add(0);
      record.add(null);
      if (r2.get(i).size() == 0) {
        flag = false;
        break;
      }
    }
    idx.set(r2.size() - 1, -1);
    while (flag) {
      int i = r2.size() - 1;
      idx.set(i, idx.get(i) + 1);
      for (; i >= 0 && idx.get(i).equals(r2.get(i).size()); --i) {
        idx.set(i, 0);
        if (i == 0) {
          --i;
          break;
        }
        idx.set(i - 1, idx.get(i - 1) + 1);
      }
      if(i < 0) break;
      for (i = 0; i < r2.size(); ++i)
        record.set(i, r2.get(i).getRecord(idx.get(i)));

      if (we.eval(record).isTrue()) {
        List<Integer> tmp = new ArrayList<Integer>();
        for (int j : idx) {
          tmp.add(j);
        }
        idxes.add(tmp);
      }
    }

    if (sc.size() == 0) {
      for (int i = 0; i < r2.size(); ++i) {
        List<Column> lc = r2.get(i).getTable().getSortedColumns();
        for (int j = 0; j < lc.size(); ++j) {
          cc.add(new Coord(i, j));
          sc.add(new Foreign(null, lc.get(j).getName()));
          cAlias.add(null);
        }
      }
    }

    for (int i = 0; i < sc.size(); ++i) {
      System.out.print('+');
      for (int j = 0; j < 22; ++j)
        System.out.print('-');
    }
    System.out.println('+');
    for (int i = 0; i < sc.size(); ++i) {
      System.out.print("| ");
      System.out.print(String.format("%-20.20s",
        cAlias.get(i) == null ? sc.get(i).getColumnName() : cAlias.get(i)));
      System.out.print(' ');
    }
    System.out.println('|');
    for (int i = 0; i < sc.size(); ++i) {
      System.out.print('+');
      for (int j = 0; j < 22; ++j)
        System.out.print('-');
    }
    System.out.println('+');
    for (List<Integer> z : idxes) {
      for (int i = 0; i < sc.size(); ++i) {
        System.out.print("| ");
        Coord coord = cc.get(i);
        System.out.print(String.format("%-20.20s", r2.get(coord.getX()).getRecord(z.get(coord.getX())).get(coord.getY())));
        System.out.print(' ');
      }
      System.out.println('|');
    }
    for (int i = 0; i < sc.size(); ++i) {
      System.out.print('+');
      for (int j = 0; j < 22; ++j)
        System.out.print('-');
    }
    System.out.println('+');
  }
}
