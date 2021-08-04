package SQLDumps;

import UserAuthentication.User;

import java.io.*;
import java.util.ArrayList;

import static Processor.ManageMetaData.*;

public class SQLDump {

  public static void generateSQLDump(User user) {
    if (user.getDatabasePath() != null) {
      System.out.println("\n<=== CREATE SCHEMA ===>");
      System.out.println("# CREATE DATABASE " + user.getDatabase());
      createTableDump(user);
    } else {
      System.out.println("Please Select Database First !!!");
    }
  }

  public static void createTableDump(User user) {
    System.out.println("\n<=== CREATE TABLES ===>");
    ArrayList<String> tableList = getTableList(user);
    if (tableList.isEmpty()) {
      System.out.println("No Tables Created yet !!!");
      return;
    }
    for (String tableName : tableList) {
      StringBuilder createQuery = new StringBuilder("# CREATE TABLE " + tableName);
      ArrayList<String> columns = getColumnsMeta(tableName, user);
      if (!columns.isEmpty()) {
        createQuery.append(" (");
        for (String col : columns) {
          createQuery.append(col).append(", ");
        }
        String pk = getPrimaryKey(tableName, user);
        if (pk != null) {
          createQuery.append("PRIMARY KEY (").append(pk).append("), ");
        }
        ArrayList<String> foreignKeys = getForeignKeys(tableName, user);
        if (!foreignKeys.isEmpty()) {
          for (String fk : foreignKeys) {
            createQuery.append(fk).append(", ");
          }
        }
        createQuery = new StringBuilder(createQuery.substring(0, createQuery.length() - 2));
        createQuery.append(");");
        System.out.println(createQuery);
      }
    }
    insertTableDump(user);
  }

  public static void insertTableDump(User user) {
    System.out.println("\n<=== INSERT RAWS ===>");
    for (String table : getTableList(user)) {
      System.out.println("TABLE : " + table);
      ArrayList<String> listOfRecords = isDataExist(table, user);
      if (!listOfRecords.isEmpty()) {

        for (int i = 1; i < listOfRecords.size(); i++) {
          String columnName = listOfRecords.get(0).replaceAll(",", ", ");
          String insertQuery = "# INSERT INTO " + table + " (" + columnName + ") VALUES (";
          insertQuery += listOfRecords.get(i).replaceAll(",", ", ");
          insertQuery += ");";
          System.out.println(insertQuery);
        }

      } else {
        System.out.println("No Records Inserted yet !!!");
      }
      System.out.print("\n");
    }
  }

  public static ArrayList<String> isDataExist(String tableName, User user) {
    ArrayList<String> listOfRecords = new ArrayList<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(user.getDatabasePath() + "table_" + tableName + ".txt"));
      int lines = 0;
      String line;
      while ((line = br.readLine()) != null) {
        if (line.trim().length() > 0) {
          lines++;
          listOfRecords.add(line.trim());
        }
      }
      if (lines > 1) {
        return listOfRecords;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return listOfRecords;
  }

  public static void printDatabaseState(User user) {
    if (user.getDatabasePath() != null) {
      System.out.println("\nCURRENT DATABASE : " + user.getDatabase());
      ArrayList<String> tableList = getTableList(user);
      if (tableList.isEmpty()) {
        System.out.println("No Tables Created yet !!!");
        return;
      }
      for (String tableName : tableList) {
        int lines = 0;
        try {
          BufferedReader br = new BufferedReader(new FileReader(user.getDatabasePath() + "table_" + tableName + ".txt"));
          String line;
          while ((line = br.readLine()) != null) {
            if (line.trim().length() > 0) {
              lines++;
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        System.out.println("TABLE : " + tableName);
        if (lines < 1) {
          System.out.println("NO RECORDS");
        }
        System.out.println("TOTAL RAWS : " + (lines - 1) + "\n");
      }
    } else {
      System.out.println("Please Select Database First !!!");
    }
  }
}
