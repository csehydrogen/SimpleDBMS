import java.util.ArrayList;
import java.util.List;

public class ExpTree {
  public static interface CompOperand {
    public void getForeigns(List<Foreign> lf);
    public Value getValue(List<List<Value>> record);
  }

  public static class ComparisonPredicate implements Predicate {
    private static final int LT = 0; // <
    private static final int GT = 1; // >
    private static final int EQ = 2; // =
    private static final int GE = 3; // >=
    private static final int LE = 4; // <=
    private static final int NE = 5; // !=

    private CompOperand leftOp;
    private int compOp;
    private CompOperand rightOp;

    public void setLeftOp(CompOperand leftOp) { this.leftOp = leftOp; }
    public void setRightOp(CompOperand rightOp) { this.rightOp = rightOp; }
    public void setCompOp(String compOp) {
      switch(compOp) {
        case "<": this.compOp = LT; break;
        case ">": this.compOp = GT; break;
        case "=": this.compOp = EQ; break;
        case ">=": this.compOp = GE; break;
        case "<=": this.compOp = LE; break;
        case "!=": this.compOp = NE; break;
      }
    }

    public void getForeigns(List<Foreign> lf) {
      leftOp.getForeigns(lf);
      rightOp.getForeigns(lf);
    }

    public Value3 eval(List<List<Value>> record) throws ParseException {
      Value leftV = leftOp.getValue(record);
      Value rightV = rightOp.getValue(record);
      if (leftV.getType() == Value.NULL || rightV.getType() == Value.NULL)
        return new Value3(Value3.UNKNOWN);
      if (leftV.getType() != rightV.getType()) {
        Message.print(Message.WHERE_INCOMPARABLE_ERROR);
        throw new ParseException();
      }
      if (leftV.getType() == Value.INT) {
        switch (compOp) {
          case LT: return new Value3(leftV.getValInt() < rightV.getValInt() ? Value3.TRUE : Value3.FALSE);
          case GT: return new Value3(leftV.getValInt() > rightV.getValInt() ? Value3.TRUE : Value3.FALSE);
          case EQ: return new Value3(leftV.getValInt() == rightV.getValInt() ? Value3.TRUE : Value3.FALSE);
          case GE: return new Value3(leftV.getValInt() >= rightV.getValInt() ? Value3.TRUE : Value3.FALSE);
          case LE: return new Value3(leftV.getValInt() <= rightV.getValInt() ? Value3.TRUE : Value3.FALSE);
          case NE: return new Value3(leftV.getValInt() != rightV.getValInt() ? Value3.TRUE : Value3.FALSE);
        }
      }
      switch (compOp) {
        case LT: return new Value3(leftV.getValStr().compareTo(rightV.getValStr()) < 0 ? Value3.TRUE : Value3.FALSE);
        case GT: return new Value3(leftV.getValStr().compareTo(rightV.getValStr()) > 0 ? Value3.TRUE : Value3.FALSE);
        case EQ: return new Value3(leftV.getValStr().compareTo(rightV.getValStr()) == 0 ? Value3.TRUE : Value3.FALSE);
        case GE: return new Value3(leftV.getValStr().compareTo(rightV.getValStr()) >= 0 ? Value3.TRUE : Value3.FALSE);
        case LE: return new Value3(leftV.getValStr().compareTo(rightV.getValStr()) <= 0 ? Value3.TRUE : Value3.FALSE);
        case NE: return new Value3(leftV.getValStr().compareTo(rightV.getValStr()) != 0 ? Value3.TRUE : Value3.FALSE);
      }
      // never reach here
      return new Value3(Value3.UNKNOWN);
    }
  }

  public static class NullPredicate implements Predicate {
    private Foreign op;
    private boolean isNull;

    public void setOp(Foreign op) { this.op = op; }
    public void setIsNull(boolean isNull) { this.isNull = isNull; }
    public void getForeigns(List<Foreign> lf) {
      lf.add(op);
    }
    public Value3 eval(List<List<Value>> record) {
      SelectHelper.Coord coord = op.getCoord();
      Value v = record.get(coord.getX()).get(coord.getY());
      if (isNull) {
        if (v.getType() == Value.NULL)
          return new Value3(Value3.TRUE);
        return new Value3(Value3.FALSE);
      }
      if (v.getType() == Value.NULL)
        return new Value3(Value3.FALSE);
      return new Value3(Value3.TRUE);
    }
  }

  public static interface Predicate extends BooleanTest{
  }

  public static interface BooleanTest {
    public void getForeigns(List<Foreign> lf);
    public Value3 eval(List<List<Value>> record) throws ParseException;
  }

  public static class BooleanFactor {
    private boolean not = false;
    private BooleanTest op;

    public void setNot(boolean not) { this.not = not; }
    public void setOp(BooleanTest op) { this.op = op; }
    public void getForeigns(List<Foreign> lf) {
      op.getForeigns(lf);
    }
    public Value3 eval(List<List<Value>> record) throws ParseException {
      Value3 ret = op.eval(record);
      if(not) return ret.not();
      return ret;
    }
  }

  public static class BooleanTerm {
    private List<BooleanFactor> ops = new ArrayList<BooleanFactor>();

    public void addOp(BooleanFactor op) { ops.add(op); }
    public void getForeigns(List<Foreign> lf) {
      for (BooleanFactor op : ops) op.getForeigns(lf);
    }
    public Value3 eval(List<List<Value>> record) throws ParseException {
      Value3 ret = new Value3(Value3.TRUE);
      for (BooleanFactor op : ops)
        ret = ret.and(op.eval(record));
      return ret;
    }
  }

  public static class BooleanValueExpression implements BooleanTest, WhereExpression {
    private List<BooleanTerm> ops = new ArrayList<BooleanTerm>();

    public void addOp(BooleanTerm op) { ops.add(op); }
    public void getForeigns(List<Foreign> lf) {
      for (BooleanTerm op : ops) op.getForeigns(lf);
    }
    public Value3 eval(List<List<Value>> record) throws ParseException {
      Value3 ret = new Value3(Value3.FALSE);
      for (BooleanTerm op : ops)
        ret = ret.or(op.eval(record));
      return ret;
    }
  }

  public static class TrueExpression implements WhereExpression {
    public void getForeigns(List<Foreign> lf) {}
    public Value3 eval(List<List<Value>> record) throws ParseException { return new Value3(Value3.TRUE); }
  }

  public static interface WhereExpression {
    public void getForeigns(List<Foreign> lf);
    public Value3 eval(List<List<Value>> record) throws ParseException;
  }

  public static class Value3 {
    public static final int TRUE = 0;
    public static final int FALSE = 1;
    public static final int UNKNOWN = 2;

    private int val;
    public boolean isTrue() { return val == TRUE; }
    public Value3(int val) { this.val = val; }
    public Value3 and(Value3 that) {
      if (val == UNKNOWN) {
        if (that.val == FALSE)
          return new Value3(FALSE);
        return new Value3(UNKNOWN);
      }
      if (that.val == UNKNOWN) {
        if (val == FALSE)
          return new Value3(FALSE);
        return new Value3(UNKNOWN);
      }
      if (val == TRUE && that.val == TRUE)
        return new Value3(TRUE);
      return new Value3(FALSE);
    }
    public Value3 or(Value3 that) {
      if (val == UNKNOWN) {
        if (that.val == TRUE)
          return new Value3(TRUE);
        return new Value3(UNKNOWN);
      }
      if (that.val == UNKNOWN) {
        if (val == TRUE)
          return new Value3(TRUE);
        return new Value3(UNKNOWN);
      }
      if (val == TRUE || that.val == TRUE)
        return new Value3(TRUE);
      return new Value3(FALSE);
    }
    public Value3 not() {
      if (val == UNKNOWN)
        return new Value3(UNKNOWN);
      if (val == TRUE)
        return new Value3(FALSE);
      return new Value3(TRUE);
    }
  }
}
