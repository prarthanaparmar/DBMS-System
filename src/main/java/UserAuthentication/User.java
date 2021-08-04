package UserAuthentication;

public class User {
  private String userName;
  private String password;
  private String answer;
  private String database;
  private String databasePath;
  private boolean isValid;

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getDatabase() {
    return this.database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getDatabasePath() {
    return this.databasePath;
  }

  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
  }

  public boolean getValid() {
    return this.isValid;
  }

  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }
}