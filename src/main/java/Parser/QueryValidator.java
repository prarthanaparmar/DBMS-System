package Parser;

import UserAuthentication.User;
import Processor.QueryProcessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Processor.ManageMetaData.*;
import static Processor.QueryProcessor.deleteProcessor;
import static Processor.QueryProcessor.insertProcessor;

public class QueryValidator {

  static final String INVALID_QUERY = "Invalid Query !!!";
  private static final ArrayList<String> queryTypes = new ArrayList<>(Arrays.asList("USE", "CREATE", "SELECT", "INSERT", "UPDATE", "DELETE"));
  private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  public static void runQuery(User user) {
    String query = inputQuery();
    FileWriter fw;
    try {
      fw = new FileWriter("src/queryLogs.txt", true);
      fw.write("Query : " + query + "\n");
      fw.write("User : " + user.getUserName() + "\n");
      fw.write("Time of Submission : " + dtf.format(LocalDateTime.now()) + "\n");
      long qTime = System.currentTimeMillis();
      String queryType = getQueryType(query);
      String message = null;
      if (!queryType.equals(INVALID_QUERY)) {
        message = checkForParser(query, queryType, user);
        fw.write("Message : " + message + "\n");
      } else {
        fw.write("Message : " + INVALID_QUERY + "\n");
      }
      fw.write("Total Time Elapsed : " + (System.currentTimeMillis() - qTime) + " ms\n\n");
      fw.close();
      System.out.println("Message: " + message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String inputQuery() {
    Scanner queryInput = new Scanner(System.in);
    System.out.print("\nEnter Query : ");
    return queryInput.nextLine().trim().toUpperCase();
  }

  public static String getQueryType(String query) {
    String[] queryGroups = query.split(" ");
    if (queryGroups.length > 0) {
      if (queryTypes.contains(queryGroups[0])) {
        return queryGroups[0].trim().toUpperCase();
      } else {
        return INVALID_QUERY;
      }
    } else {
      return INVALID_QUERY;
    }
  }

  public static String checkForParser(String query, String queryType, User user) throws IOException {
    String result = "";
    if (user.getDatabasePath() != null) {
      switch (queryType) {
        case "USE":
          result = useDbParser(query, user);
          break;
        case "CREATE":
          String[] queryKeywords = query.split(" ");
          if (queryKeywords.length > 2) {
            if (queryKeywords[1].equalsIgnoreCase("database")) {
              result = createDBParser(query);
            } else {
              result = createParser(query, user);
            }
          } else {
            result = INVALID_QUERY;
          }
          break;
        case "SELECT":
          result = selectParser(query, user);
          break;
        case "INSERT":
          result = insertParser(query, user);
          break;
        case "UPDATE":
          result = updateParser(query, user);
          break;
        case "DELETE":
          result = deleteParser(query, user);
          break;
      }
    } else {
      if (queryType.equals("USE") || queryType.equals("CREATE")) {
        String[] queryKeywords = query.split(" ");
        if (queryKeywords.length > 2) {
          if (queryKeywords[1].equalsIgnoreCase("database")) {
            if (queryType.equals("USE")) {
              result = useDbParser(query, user);
            } else {
              result = createDBParser(query);
            }
          } else {
            result = "Please Select Database First !!!";
          }
        } else {
          result = INVALID_QUERY;
        }

      } else {
        result = "Please Select Database First !!!";
      }
    }
    return result;
  }

  public static String createDBParser(String query) {
    String returnMessage = "";
    String finalRegex = "CREATE DATABASE\\s(\\w+);?$";

    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    if (matcher.find()) {
      String dataBaseName = matcher.group(1);
      if (dataBaseName != null) {
        if (!checkDBExist(dataBaseName.trim().toUpperCase())) {
          try {
            File file = new File("src/rawDBFiles/" + dataBaseName.trim().toUpperCase() + "_DB");
            boolean flag = file.mkdir();
            if (flag) {
              String fileName = "src/rawDBFiles/" + dataBaseName.trim().toUpperCase() + "_DB/dbMetaData.txt";
              FileWriter fileWriter = new FileWriter(fileName, true);
              fileWriter.close();

              FileWriter fw = new FileWriter("src/dbList.txt", true);
              fw.write(dataBaseName.trim().toUpperCase() + "\n");
              fw.close();
              returnMessage = "Database Created Successfully !!!";
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          returnMessage = "Database already present !!!";
        }
      }
    } else {
      returnMessage = INVALID_QUERY;
    }
    return returnMessage;
  }

  public static String useDbParser(String query, User user) {
    String returnMessage;
    String finalRegex = "USE DATABASE\\s(\\w+);?$";
    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    if (matcher.find()) {
      String dataBaseName = matcher.group(1);
      if (dataBaseName != null) {
        if (checkDBExist(dataBaseName.trim().toUpperCase())) {
          user.setDatabasePath("src/rawDBFiles/" + dataBaseName.trim().toUpperCase() + "_DB/");
          user.setDatabase(dataBaseName.trim().toUpperCase());
          returnMessage = dataBaseName + " Database Selected !!!";
        } else {
          returnMessage = "Invalid Database !!!";
        }
      } else {
        returnMessage = INVALID_QUERY;
      }
    } else {
      returnMessage = INVALID_QUERY;
    }
    return returnMessage;
  }

  public static String createParser(String query, User user) {
    String returnMessage;
    String primaryKeyRegex = "(?:,\\sPRIMARY KEY\\s[(](\\w+)[)])?";
    String foreignKeyRegex = "((?:,\\sFOREIGN KEY\\s[(](\\w+)[)]\\sREFERENCES\\s(\\w+)[(](\\w+)[)])+)?";
    String columnsRegex = "[(]((((\\w+)\\s(varchar|int)[(]\\d+[)])(,)*\\s*)+)" + primaryKeyRegex + foreignKeyRegex + "[)]";
    String finalRegex = "CREATE TABLE\\s(\\w+)\\s*" + columnsRegex + ";?$";

    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    Map<String, Map<String, ArrayList<String>>> createMetaMap = new HashMap<>();
    if (matcher.find()) {
      String tableName = matcher.group(1);
      if (tableName != null) {
        boolean isExist = checkTableExist(tableName, user);
        if (!isExist) {
          createMetaMap.put(tableName, new HashMap<>());

          String columnsString = matcher.group(2);
          ArrayList<String> columnNames = new ArrayList<>();
          if (columnsString != null) {
            ArrayList<String> columns = new ArrayList<>();
            for (String col : columnsString.split(",")) {
              columnNames.add(col.trim().split(" ")[0]);
              columns.add(col.trim());
            }
            createMetaMap.get(tableName).put("columns", columns);
          }
          createMetaMap.get(tableName).put("primaryKey", new ArrayList<>());
          createMetaMap.get(tableName).put("foreignKey", new ArrayList<>());

          String primaryKey = matcher.group(8);
          if (primaryKey != null) {
            if (columnNames.contains(primaryKey.trim())) {
              createMetaMap.get(tableName).put("primaryKey", new ArrayList<>(Collections.singletonList(primaryKey.trim())));
            } else {
              returnMessage = "Invalid Primary Key !!!";
              return returnMessage;
            }
          }

          String foreignKey = matcher.group(9);
          if (foreignKey != null) {
            ArrayList<String> foreignKeys = new ArrayList<>();
            for (String fk : foreignKey.split(",\\s")) {
              if (fk.trim().length() > 0) {
                String[] fkParts = fk.split("\\s");
                if (fkParts.length > 4) {
                  String fkNew = fkParts[2].trim().split("\\(")[1].trim();
                  fkNew = fkNew.substring(0, fkNew.length() - 1);
                  if (columnNames.contains(fkNew.trim())) {
                    String fkTable = fkParts[4].split("\\(")[0].trim();
                    if (checkTableExist(fkTable, user)) {
                      String fkReference = fkParts[4].split("\\(")[1].trim();
                      fkReference = fkReference.substring(0, fkReference.length() - 1);
                      String actualReference = getPrimaryKey(fkTable, user);
                      if (actualReference != null) {
                        if (actualReference.equalsIgnoreCase(fkReference)) {
                          String fkFile = fkNew + ":" + fkTable + ":" + fkReference;
                          foreignKeys.add(fkFile);
                        } else {
                          returnMessage = "Invalid Foreign Reference Field : " + fkReference;
                          return returnMessage;
                        }
                      } else {
                        returnMessage = "Invalid Foreign Reference Field : " + fkReference;
                        return returnMessage;
                      }
                    } else {
                      returnMessage = "Invalid Foreign Reference Table : " + fkTable;
                      return returnMessage;
                    }
                  } else {
                    returnMessage = "Invalid Foreign Key : " + fkNew;
                    return returnMessage;
                  }
                }
              }
            }
            if (!foreignKeys.isEmpty()) {
              createMetaMap.get(tableName).put("foreignKey", foreignKeys);
            }
          }
          return QueryProcessor.createProcessor(createMetaMap, user);
        } else {
          returnMessage = "Table already Exist !!!";
          return returnMessage;
        }
      } else {
        return INVALID_QUERY;
      }
    } else {
      return INVALID_QUERY;
    }
  }

  public static String selectParser(String query, User user) {
    String returnMessage = "";
    String conditionRegex = "(?:(?:\\sWHERE\\s)(?:(\\w+)\\s*(=|!=)\\s*(\".*\"|\\d+(?:.\\d+)?|TRUE|FALSE|true|false)))?;?$";
    String finalRegex = "SELECT\\s((?:\\*)|(?:(?:\\w+)(?:,\\s*\\w+)*))\\sFROM\\s(\\w+)" + conditionRegex;

    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    Map<String, String> selectMetaMap = new HashMap<>();
    if (matcher.find()) {

      String columnsString = matcher.group(1);
      String tableName = matcher.group(2);

      if (checkTableExist(tableName, user)) {
        if (!isTableAcquired(tableName.trim(), user)) {
          selectMetaMap.put("tableName", tableName);

          ArrayList<String> allColumns = getColumnNames(tableName, user);
          if (columnsString != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (columnsString.equals("*")) {
              for (String col : allColumns) {
                stringBuilder.append(col).append("|");
              }
            } else {
              for (String col : columnsString.split(",")) {
                if (!allColumns.contains(col.trim())) {
                  returnMessage = "Column " + col.trim() + " does not exist in the table";
                  return returnMessage;
                }
                stringBuilder.append(col.trim()).append("|");
              }
            }
            selectMetaMap.put("columns", String.valueOf(stringBuilder));
          }

          String conditionColumn = matcher.group(3);
          if (conditionColumn != null) {
            if (!allColumns.contains(conditionColumn)) {
              returnMessage = "Condition Column not exist in table !!!";
              return returnMessage;
            }
            selectMetaMap.put("conditionColumn", conditionColumn);

            String conditionOperator = matcher.group(4);
            if (conditionOperator != null) {
              selectMetaMap.put("conditionOperator", conditionOperator);
            }

            String conditionValue = matcher.group(5);
            if (conditionValue != null) {
              selectMetaMap.put("conditionValue", conditionValue);
            }
          }
          return QueryProcessor.selectProcessor(selectMetaMap, user);
        } else {
          returnMessage = "Table is in use by other user !!!";
          return returnMessage;
        }
      } else {
        returnMessage = "Table not Exist !!!";
        return returnMessage;
      }
    } else {
      return INVALID_QUERY;
    }
  }

  public static String insertParser(String query, User user) {
    String returnMessage = "";
    String columnNameRegex = "(\\((?:\\w+)(?:,\\s?\\w+)*\\))?";
    String valueRegex = "(?:\".*\"|\\d+?|TRUE|FALSE|true|false)";
    String columnValueRegex = "(\\((?:" + valueRegex + ")(?:,\\s?" + valueRegex + ")*\\))";
    String finalRegex = "INSERT INTO (\\w+)\\s" + columnNameRegex + "\\sVALUES\\s" + columnValueRegex + ";?$";

    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    Map<String, String> insertMetaMap = new HashMap<>();
    if (matcher.find()) {
      String tableName = matcher.group(1);
      if (checkTableExist(tableName, user)) {
        if (!isTableAcquired(tableName.trim(), user)) {
          // APPLY LOCK TO TABLE
          lockTable(tableName, user);

          //INSERT LOGIC HERE
          insertMetaMap.put("tableName", tableName);

          List<String> allColumns = getColumnNames(tableName, user);
          String columns = matcher.group(2);
          if (columns != null) {
            columns = columns.substring(1, columns.length() - 1);
            for (String col : columns.split(",")) {
              if (!allColumns.contains(col.trim())) {
                returnMessage = "Column " + col.trim() + " does not exist in the table";
                unlockTable(tableName, user);
                return returnMessage;
              }
            }
            insertMetaMap.put("columns", columns);
          }

          String values = matcher.group(3);
          if (values != null) {
            values = values.substring(1, values.length() - 1);
            insertMetaMap.put("values", values);
          }

          returnMessage = insertProcessor(insertMetaMap, user);

          //UNLOCK TABLE
          unlockTable(tableName, user);
        } else {
          returnMessage = "Table is in use by other user !!!";
        }
      } else {
        returnMessage = "Table does not exist";
      }
    } else {
      return INVALID_QUERY;
    }
    return returnMessage;
  }

  public static String updateParser(String query, User user) {
    String returnMessage = "";
    String valueTypes = "(?:\".*\"|\\d+(?:.\\d+)?|TRUE|FALSE|true|false)";
    String assignmentRegex = "((?:\\w+\\s*=\\s*" + valueTypes + ")(?:,\\s*\\w+\\s*=\\s*" + valueTypes + ")*)\\s*";
    String conditionRegex = "(?:(?:\\sWHERE\\s)(?:(\\w+)\\s*(=|!=)\\s*(\".*\"|\\d+(?:.\\d+)?|TRUE|FALSE|true|false)))?;?$";
    String finalRegex = "UPDATE\\s(\\w+)\\sSET\\s" + assignmentRegex + conditionRegex + ";?$";

    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    String colName = null;
    String colValue = null;
    Map<String, String> updateMetaMap = new HashMap<>();
    if (matcher.find()) {
      String tableName = matcher.group(1);
      if (checkTableExist(tableName, user)) {
        if (!isTableAcquired(tableName.trim(), user)) {
          // APPLY LOCK TO TABLE
          lockTable(tableName, user);

          updateMetaMap.put("tableName", tableName);
          ArrayList<String> allColumns = getColumnNames(tableName, user);
          String updateColumn = matcher.group(2);
          if (updateColumn != null) {
            String[] updateColumns = updateColumn.split(",");
            for (String col : updateColumns) {
              if (col.contains("!")) {
                colName = col.trim().split("!=")[0].trim();
                colValue = col.trim().split(("!="))[1].trim();
              } else {
                colName = col.trim().split("=")[0].trim();
                colValue = col.trim().split(("="))[1].trim();
              }
              if (!allColumns.contains(colName)) {
                returnMessage = "Column " + colName + " not exist in table";
                unlockTable(tableName, user);
                return returnMessage;
              }
              updateMetaMap.put("colValue", colValue);
              updateMetaMap.put("colName", colName);
            }
          }
          String conditionColumn = matcher.group(3);
          if (conditionColumn != null) {
            if (!allColumns.contains(conditionColumn)) {
              returnMessage = "Condition Column not exist in table !!!";
              unlockTable(tableName, user);
              return returnMessage;
            }
            updateMetaMap.put("conditionColumn", conditionColumn);

            String conditionOperator = matcher.group(4);
            if (conditionOperator != null) {
              updateMetaMap.put("conditionOperator", conditionOperator);
            }

            String conditionValue = matcher.group(5);
            if (conditionValue != null) {
              updateMetaMap.put("conditionValue", conditionValue);
            }
          }
          returnMessage = QueryProcessor.updateProcessor(updateMetaMap, user);

          // UNLOCK TABLE
          unlockTable(tableName, user);
        } else {
          returnMessage = "Table is in use by other user !!!";
        }
      } else {
        returnMessage = "Table not Exist !!!";
        return returnMessage;
      }
    } else {
      return INVALID_QUERY;
    }
    return returnMessage;
  }

  public static String deleteParser(String query, User user) throws IOException {
    String returnMessage = "";
    String conditionValues = "(\".*\"|\\d+(?:.\\d+)?|TRUE|FALSE|true|false)";
    String conditionOperators = "(=|!=|<|<=|>=|>|)";
    String conditionRegex = "(?:(?:\\sWHERE\\s)(?:(\\w+)" + "\\s?" + (conditionOperators) + "\\s?" + conditionValues + "))?";
    String finalRegex = "DELETE FROM\\s(\\w+)" + conditionRegex + ";?$";

    Pattern pattern = Pattern.compile(finalRegex, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(query);
    Map<String, String> deleteMetaMap = new HashMap<>();
    if (matcher.find()) {
      String tableName = matcher.group(1);
      if (!isTableAcquired(tableName.trim(), user)) {
        lockTable(tableName, user);
        deleteMetaMap.put("tableName", tableName);

        String conditionColumn = matcher.group(2);
        deleteMetaMap.put("conditionColumn", conditionColumn);

        String conditionOperator = matcher.group(3);
        deleteMetaMap.put("conditionOperator", conditionOperator);

        String conditionValue = matcher.group(4);
        deleteMetaMap.put("conditionValue", conditionValue);

        returnMessage = deleteProcessor(deleteMetaMap, user);
        unlockTable(tableName, user);
      } else {
        returnMessage = "Table is in use by other user !!!";
      }
    } else {
      return INVALID_QUERY;
    }
    return returnMessage;

  }

  public static boolean isTableAcquired(String tableName, User user) {
    try {
      File file = new File("src/lockTables.txt");
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        String[] lineParts = line.split(" --> ");
        if (lineParts[0].equals(user.getDatabase()) && lineParts[1].equals(tableName.toUpperCase())) {
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static void lockTable(String tableName, User user) {
    FileWriter fw;
    try {
      fw = new FileWriter("src/lockTables.txt", true);
      fw.write(user.getDatabase() + " --> " + tableName.trim());
      fw.write(System.lineSeparator());
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void unlockTable(String tableName, User user) {
    List<String> fileContent;
    try {
      fileContent = new ArrayList<>(Files.readAllLines(Paths.get("src/lockTables.txt"), StandardCharsets.UTF_8));
      for (int i = 0; i < fileContent.size(); i++) {
        if (fileContent.get(i).equals(user.getDatabase() + " --> " + tableName.trim())) {
          fileContent.remove(i);
          break;
        }
      }
      Files.write(Paths.get("src/lockTables.txt"), fileContent, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
