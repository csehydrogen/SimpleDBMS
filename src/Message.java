public class Message {
  public static final int SYNTAX_ERROR = 0;
  public static final int CREATE_TABLE = 1;
  public static final int DROP_TABLE= 2;
  public static final int DESC = 3;
  public static final int SHOW_TABLES = 4;
  public static final int INSERT = 5;
  public static final int DELETE = 6;
  public static final int SELECT = 7;
  public static final int DUPLICATE_COLUMN_DEF_ERROR = 8;
  public static final int DUPLICATE_PRIMARY_KEY_DEF_ERROR = 9;
  public static final int NON_EXISTING_COLUMN_DEF_ERROR = 10;
  public static final int TABLE_EXISTENCE_ERROR = 11;
  public static final int REFERENCE_TYPE_ERROR = 12;
  public static final int REFERENCE_TABLE_EXISTENCE_ERROR = 13;
  public static final int REFERENCE_COLUMN_EXISTENCE_ERROR = 14;
  public static final int REFERENCE_NON_PRIMARY_KEY_ERROR = 15;
  public static final int DUPLICATE_FOREIGN_KEY_DEF_ERROR = 16;
  public static final int CHAR_LENGTH_ERROR = 17;
  public static final int CREATE_TABLE_SUCCESS = 18;
  public static final int NO_SUCH_TABLE = 19;
  public static final int EMPTY = 20;
  public static final int HORIZONTAL_LINE = 21;
  public static final int DROP_SUCCESS_ALL_TABLES = 22;
  public static final int DROP_SUCCESS = 23;
  public static final int DROP_REFERENCED_TABLE_ERROR = 24;
  public static final int INSERT_COLUMN_EXISTENCE_ERROR = 25;
  public static final int INSERT_TYPE_MISMATCH_ERROR = 26;
  public static final int INSERT_COLUMN_NON_NULLABLE_ERROR = 27;
  public static final int INSERT_DUPLICATE_PRIMARY_KEY_ERROR = 28;
  public static final int INSERT_REFERENTIAL_INTEGRITY_ERROR = 29;
  public static final int INSERT_RESULT = 30;

  // message code
  int code;
  // optional string argument
  String arg0;
  // print prompt preceding message
  boolean prompt;

  public Message(int code) {
    this(code, null, true);
  }

  public Message(int code, String arg0) {
    this(code, arg0, true);
  }

  public Message(int code, boolean prompt) {
    this(code, null, prompt);
  }

  public Message(int code, String arg0, boolean prompt) {
    this.code = code;
    this.arg0 = arg0;
    this.prompt = prompt;
  }

  public void print() {
    print(code, arg0, prompt);
  }

  public static void print(int code) {
    print(code, null, true);
  }

  public static void print(int code, String arg0) {
    print(code, arg0, true);
  }

  public static void print(int code, boolean prompt) {
    print(code, null, prompt);
  }

  public static void print(int code, String arg0, boolean prompt) {
    if(prompt)
      System.out.print("DB_2013-11395> ");
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
      case DUPLICATE_COLUMN_DEF_ERROR:
        System.out.println("Create table has failed: column definition is duplicated");
        break;
      case DUPLICATE_PRIMARY_KEY_DEF_ERROR:
        System.out.println("Create table has failed: primary key definition is duplicated");
        break;
      case NON_EXISTING_COLUMN_DEF_ERROR:
        System.out.print("Create table has failed: ‘");
        System.out.print(arg0);
        System.out.println("’ does not exists in column definition");
        break;
      case TABLE_EXISTENCE_ERROR:
        System.out.println("Create table has failed: table with the same name already exists");
        break;
      case REFERENCE_TYPE_ERROR:
        System.out.println("Create table has failed: foreign key references wrong type");
        break;
      case REFERENCE_TABLE_EXISTENCE_ERROR:
        System.out.println("Create table has failed: foreign key references non existing table");
        break;
      case REFERENCE_COLUMN_EXISTENCE_ERROR:
        System.out.println("Create table has failed: foreign key references non existing column");
        break;
      case REFERENCE_NON_PRIMARY_KEY_ERROR:
        System.out.println("Create table has failed: foreign key references non primary key column");
        break;
      case DUPLICATE_FOREIGN_KEY_DEF_ERROR:
        System.out.println("Create table has failed: foreign key definition is duplicated");
        break;
      case CHAR_LENGTH_ERROR:
        System.out.println("Char length should be > 0");
        break;
      case CREATE_TABLE_SUCCESS:
        System.out.print("‘");
        System.out.print(arg0);
        System.out.println("’ table is created");
        break;
      case NO_SUCH_TABLE:
        System.out.println("No such table");
        break;
      case EMPTY:
        break;
      case HORIZONTAL_LINE:
        System.out.println("---------------------------------------------------");
        break;
      case DROP_SUCCESS_ALL_TABLES:
        System.out.println("Every table is dropped");
        break;
      case DROP_SUCCESS:
        System.out.println(String.format("‘%s’ table is dropped", arg0));
        break;
      case DROP_REFERENCED_TABLE_ERROR:
        System.out.println(String.format("Drop table has failed: ‘%s’ is referenced by other table", arg0));
        break;
      case INSERT_COLUMN_EXISTENCE_ERROR:
        System.out.println(String.format("Insertion has failed: ‘%s’ does not exist", arg0));
        break;
      case INSERT_TYPE_MISMATCH_ERROR:
        System.out.println("Insertion has failed: Types are not matched");
        break;
      case INSERT_COLUMN_NON_NULLABLE_ERROR:
        System.out.println(String.format("Insertion has failed: ‘%s’ is not nullable", arg0));
        break;
      case INSERT_DUPLICATE_PRIMARY_KEY_ERROR:
        System.out.println("Insertion has failed: Primary key duplication");
        break;
      case INSERT_REFERENTIAL_INTEGRITY_ERROR:
        System.out.println("Insertion has failed: Referential integrity violation");
        break;
      case INSERT_RESULT:
        System.out.println("The row is inserted");
        break;
    }
  }
}
