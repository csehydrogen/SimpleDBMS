public class Message {
  public static final int SYNTAX_ERROR = 0;
  public static final int CREATE_TABLE = 1;
  public static final int DROP_TABLE= 2;
  public static final int DESC = 3;
  public static final int SHOW_TABLES = 4;
  public static final int INSERT = 5;
  public static final int DELETE = 6;
  public static final int SELECT = 7;

  public static void print(int code) {
    print(code, null);
  }

  public static void print(int code, String arg0) {
    switch(code) {
      case SYNTAX_ERROR:
        System.out.println("Syntax error");
        break;
      case CREATE_TABLE:
        System.out.println("\'CREATE TABLE\' requested");
        break;
      case DROP_TABLE:
        System.out.println("\'DROP TABLE\' requested");
        break;
      case DESC:
        System.out.println("\'DESC\' requested");
        break;
      case SHOW_TABLES:
        System.out.println("\'SHOW TABLES\' requested");
        break;
      case INSERT:
        System.out.println("\'INSERT\' requested");
        break;
      case DELETE:
        System.out.println("\'DELETE\' requested");
        break;
      case SELECT:
        System.out.println("\'SELECT\' requested");
        break;
    }
  }
}
