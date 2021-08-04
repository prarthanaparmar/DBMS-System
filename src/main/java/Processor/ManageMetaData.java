package Processor;

import UserAuthentication.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static SQLDumps.SQLDump.isDataExist;

public class ManageMetaData {
  public static boolean checkDBExist(String databaseName) {
    String fileName = "src/dbList.txt";
    File file = new File(fileName);
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        if (line.trim().equals(databaseName)) {
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  public static boolean checkTableExist(String tableName, User user) {
    String fileName = user.getDatabasePath() + "dbMetaData.txt";
    File file = new File(fileName);
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        line = line.replace("\n", "");
        String tableData = line.split(" --> ")[0];
        if (tableData.split(",")[0].trim().equalsIgnoreCase(tableName.trim().toUpperCase())) {
          return true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  public static Map<String, Map<String, ArrayList<String>>> getRawFromMetaData(User user) {
    Map<String, Map<String, ArrayList<String>>> metaToRawMap = new HashMap<>();
    String fileName = user.getDatabasePath() + "dbMetaData.txt";
    File file = new File(fileName);
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        line = line.replace("\n", "");

        String[] metaDataParts = line.split(" --> ");
        if (metaDataParts.length > 1) {
          //For Table MetaData
          String[] tableMetaDataParts = metaDataParts[0].split(",");
          String tableName = null;
          if (tableMetaDataParts.length > 0) {
            tableName = tableMetaDataParts[0].trim();
            metaToRawMap.put(tableName, new HashMap<>());
            metaToRawMap.get(tableName).put("primaryKey", new ArrayList<>());
            metaToRawMap.get(tableName).put("foreignKey", new ArrayList<>());

            if (tableMetaDataParts.length > 1) {
              String primaryKey = tableMetaDataParts[1].trim().split("->")[1].trim();
              metaToRawMap.get(tableName).put("primaryKey", new ArrayList<>(Collections.singletonList(primaryKey.trim())));
            }
            if (tableMetaDataParts.length > 2) {
              String[] foreignKeyList = tableMetaDataParts[2].trim().split("->")[1].trim().split("\\|");
              metaToRawMap.get(tableName).put("foreignKey", new ArrayList<>(Arrays.asList(foreignKeyList)));
            }
          }

          //For Column MetaData
          String[] columnMetaDataParts = metaDataParts[1].split("\\|");
          if (columnMetaDataParts.length > 0) {
            metaToRawMap.get(tableName).put("columns", new ArrayList<>(Arrays.asList(columnMetaDataParts)));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return metaToRawMap;
  }

  public static ArrayList<String> getTableList(User user) {
    return new ArrayList<>(getRawFromMetaData(user).keySet());
  }

  public static String getPrimaryKey(String tableName, User user) {
    if (getRawFromMetaData(user).containsKey(tableName.trim().toUpperCase())) {
      if (getRawFromMetaData(user).get(tableName.trim().toUpperCase()).get("primaryKey").size() > 0) {
        return getRawFromMetaData(user).get(tableName.trim().toUpperCase()).get("primaryKey").get(0);
      }
    }
    return null;
  }

  public static ArrayList<String> getForeignKeys(String tableName, User user) {
    ArrayList<String> fKeys = new ArrayList<>();
    if (getRawFromMetaData(user).containsKey(tableName.trim().toUpperCase())) {
      for (String fkCompact : getRawFromMetaData(user).get(tableName.trim().toUpperCase()).get("foreignKey")) {
        String[] fkParts = fkCompact.split(":");
        String fk = "FOREIGN KEY (" + fkParts[0] + ") REFERENCES " + fkParts[1] + "(" + fkParts[2] + ")";
        fKeys.add(fk);
      }
    }
    return fKeys;
  }

  public static ArrayList<String> getColumnsMeta(String tableName, User user) {
    ArrayList<String> columns = new ArrayList<>();
    if (getRawFromMetaData(user).containsKey(tableName.trim().toUpperCase())) {
      columns.addAll(getRawFromMetaData(user).get(tableName.trim().toUpperCase()).get("columns"));
    }
    return columns;
  }

  public static ArrayList<String> getColumnNames(String tableName, User user) {
    ArrayList<String> columns = new ArrayList<>();
    if (getRawFromMetaData(user).containsKey(tableName.trim().toUpperCase())) {
      for (String col : getRawFromMetaData(user).get(tableName.trim().toUpperCase()).get("columns")) {
        columns.add(col.split(" ")[0].trim().toUpperCase());
      }
    }
    return columns;
  }

  public static List<Map<String, String>> getDataFromRaw(String tableName, User user) {
    List<Map<String, String>> resultData = new ArrayList<>();
    List<String> listOfRecords = isDataExist(tableName, user);

    if (!listOfRecords.isEmpty()) {
      String[] columns = listOfRecords.get(0).split(",");
      for (int i = 1; i < listOfRecords.size(); i++) {
        Map<String, String> mapColValue = new HashMap<>();
        String[] values = new String[columns.length];
        String[] rowValues = listOfRecords.get(i).split(",");
        for (int j = 0; j < rowValues.length; j++) {
          if (rowValues[j].equals("")) {
            values[j] = null;
          } else {
            values[j] = rowValues[j];
          }
        }
        for (int j = 0; j < columns.length; j++) {
          if (columns[j] != null) {
            mapColValue.put(columns[j], values[j]);
          }
        }
        resultData.add(mapColValue);
      }
    }
    return resultData;
  }

  public static boolean isPrimaryKeyPresent(String tableName, String columnName, String columnValue, User user) {
    List<Map<String, String>> resultData = getDataFromRaw(tableName, user);
    if (!resultData.isEmpty()) {
      for (Map<String, String> mapColVal : resultData) {
        if (mapColVal.get(columnName).equals(columnValue)) {
          return true;
        }
      }
    }
    return false;
  }
}
