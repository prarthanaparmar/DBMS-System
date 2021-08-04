package Processor;

import UserAuthentication.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static Processor.ManageMetaData.*;

public class QueryProcessor {

  public static String createProcessor(Map<String, Map<String, ArrayList<String>>> createMetaMap, User user) {
    String returnMessage = "";
    for (String tableName : createMetaMap.keySet()) {
      String tableMetaData = "";
      StringBuilder columnMeta = new StringBuilder();
      for (String col : createMetaMap.get(tableName).get("columns")) {
        columnMeta.append(col).append("|");
      }

      String primaryKey = "";
      if (createMetaMap.get(tableName).get("primaryKey").size() > 0) {
        primaryKey = ",PRIMARY_KEY->" + createMetaMap.get(tableName).get("primaryKey").get(0);
      }

      StringBuilder foreignKey = new StringBuilder();
      if (createMetaMap.get(tableName).get("foreignKey").size() > 0) {
        foreignKey.append(",FOREIGN_KEY->");
        for (String fk : createMetaMap.get(tableName).get("foreignKey")) {
          foreignKey.append(fk).append("|");
        }
      }

      tableMetaData += tableName + primaryKey + foreignKey + " --> " + columnMeta;
      String fileName = user.getDatabasePath() + "dbMetaData.txt";
      try {
        FileWriter fw = new FileWriter(fileName, true);
        fw.write(tableMetaData.toUpperCase());
        fw.write(System.lineSeparator());
        fw.close();

        FileWriter fileWriter = new FileWriter(user.getDatabasePath() + "table_" + tableName + ".txt", true);
        fileWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      returnMessage = "Table Created Successfully !!!";
      return returnMessage;
    }
    return returnMessage;
  }

  public static String selectProcessor(Map<String, String> selectMetaMap, User user) {
    String returnMessage = "";
    String tableName = selectMetaMap.get("tableName");
    List<Map<String, String>> resultData = getDataFromRaw(tableName, user);
    List<Map<String, String>> finalData = new ArrayList<>();
    String[] columns = selectMetaMap.get("columns").split("\\|");
    String conditionCol = null;
    String conditionOperator = null;
    String conditionValue = null;

    if (selectMetaMap.containsKey("conditionColumn")) {
      conditionCol = selectMetaMap.get("conditionColumn");
      if (selectMetaMap.containsKey("conditionOperator")) {
        conditionOperator = selectMetaMap.get("conditionOperator");
      }
      if (selectMetaMap.containsKey("conditionValue")) {
        conditionValue = selectMetaMap.get("conditionValue");
      }
    }

    List<String> columnList = new ArrayList<>(Arrays.asList(columns));
    if (!resultData.isEmpty()) {
      for (Map<String, String> mapColVal : resultData) {
        Map<String, String> mapColValFinal = new HashMap<>();
        if (mapColVal.containsKey(conditionCol)) {
          if (conditionOperator != null) {
            if (conditionOperator.equals("=")) {
              if (mapColVal.get(conditionCol).equals(conditionValue)) {
                for (String col : mapColVal.keySet()) {
                  if (columnList.contains(col)) {
                    mapColValFinal.put(col, mapColVal.get(col));
                  }
                }
                finalData.add(mapColValFinal);
              }
            } else if (conditionOperator.equals("!=")) {
              if (!mapColVal.get(conditionCol).equals(conditionValue)) {
                for (String col : mapColVal.keySet()) {
                  if (columnList.contains(col)) {
                    mapColValFinal.put(col, mapColVal.get(col));
                  }
                }
                finalData.add(mapColValFinal);
              }
            }
          }
        } else {
          for (String col : mapColVal.keySet()) {
            if (columnList.contains(col)) {
              mapColValFinal.put(col, mapColVal.get(col));
            }
          }
          finalData.add(mapColValFinal);
        }
      }

      if (!finalData.isEmpty()) {
        for (String column : columns) {
          if (finalData.get(0).containsKey(column)) {
            System.out.print(column + "\t\t");
          }
        }
        System.out.print("\n");
        for (Map<String, String> mapColVal : finalData) {
          for (String column : columns) {
            if (mapColVal.containsKey(column)) {
              System.out.print(mapColVal.get(column) + "\t\t");
            }
          }
          System.out.print("\n");
        }
        returnMessage = finalData.size() + " Rows found";
      } else {
        returnMessage = "No Matching Data Found in table";
      }
    } else {
      returnMessage = "No Data Exist in table";
    }
    return returnMessage;
  }

  public static String updateProcessor(Map<String, String> updateMetaMap, User user) {
    String returnMessage = "";
    String tableName = updateMetaMap.get("tableName");

    int compareAt = 100;
    int replaceAt = 100;
    FileReader fr = null;
    try {
      File f = new File(user.getDatabasePath() + "table_" + tableName.trim().toUpperCase() + ".txt");
      File f2 = new File("src/rawDBFiles/Temp/" + "table_" + tableName + ".txt");
      f2.createNewFile();
      fr = new FileReader(f);
      BufferedReader br = new BufferedReader(fr);
      String line = br.readLine();

      FileWriter writerf2 = new FileWriter(f2);
      writerf2.write(line);
      writerf2.write("\n");
      String[] labels = line.split(",");

      for (int i = 0; i < labels.length; i++) {
        if (labels[i].contentEquals(updateMetaMap.get("conditionColumn"))) {
          compareAt = i;
        }
      }
      for (int i = 0; i < labels.length; i++) {
        if (labels[i].contentEquals(updateMetaMap.get("colName"))) {
          replaceAt = i;
        }
      }
      String lineFromFile = "";
      while ((lineFromFile = br.readLine()) != null) {
        lineFromFile = lineFromFile.toUpperCase(Locale.ROOT);
        String[] lineBreakDown = lineFromFile.split(",");
        if (updateMetaMap.get("conditionOperator").equals("=")) {
          if (lineBreakDown[compareAt].contentEquals(updateMetaMap.get("conditionValue"))) {
            lineBreakDown[replaceAt] = updateMetaMap.get("colValue");
            for (int i = 0; i < lineBreakDown.length; i++) {
              writerf2.write(lineBreakDown[i]);
              if (i == (labels.length) - 1) {
                writerf2.write("\n");
              } else {
                writerf2.write(",");
              }
            }
          } else {
            writerf2.write(lineFromFile);
            writerf2.write("\n");
          }
        } else if (updateMetaMap.get("conditionOperator").contentEquals("!=")) {
          if (lineBreakDown[compareAt].contentEquals(updateMetaMap.get("conditionValue"))) {
            writerf2.write(lineFromFile);
            writerf2.write("\n");
          } else {
            lineBreakDown[replaceAt] = updateMetaMap.get("colValue");
            for (int i = 0; i < lineBreakDown.length; i++) {
              writerf2.write(lineBreakDown[i]);
              if (i == (labels.length) - 1) {
                writerf2.write("\n");
              } else {
                writerf2.write(",");
              }
            }
          }
        }
      }
      fr.close();
      boolean renameSuccessful = f2.renameTo(f);
      writerf2.close();
      f.delete();
      Path temp = Files.move
              (Paths.get(f2.getAbsolutePath()),
                      Paths.get(user.getDatabasePath() + "//table_" + tableName + ".txt"));
      returnMessage = "UPDATE PROCESS COMPLETED SUCCESSFULLY";
    } catch (IOException e) {
      e.printStackTrace();
    }
    return returnMessage;
  }

  public static String insertProcessor(Map<String, String> insertMetaMap, User user) {
    String path = user.getDatabasePath();
    String table = insertMetaMap.get("tableName").trim();
    String tableFileName = "table_" + table + ".txt";
    String tableNameWithPath = path.concat(tableFileName);
    String primaryKey = getPrimaryKey(table, user);
    try {
      boolean isTableEmpty = true;
      FileReader reader = new FileReader(tableNameWithPath);
      Scanner sc = new Scanner(reader);
      if (sc.hasNext()) {
        isTableEmpty = false;
      }
      sc.close();
      reader.close();

      Map<String, Object> inputs = new HashMap<>();
      String getColumn = insertMetaMap.get("columns");
      String[] columns = getColumn.split(",");

      String getValues = insertMetaMap.get("values");
      String[] values = getValues.split(",");

      for (int i = 0; i < columns.length; i++) {
        inputs.put(columns[i].trim(), values[i].trim());
      }

      // primary key checks
      if (primaryKey != null) {
        if (!inputs.containsKey(primaryKey)) {
          return "Missing primary key column!!!";
        } else if (inputs.get(primaryKey).equals("") || inputs.get(primaryKey) == null || inputs.get(primaryKey).equals("0")) {
          return "Invalid value for primary key column";
        }
      }

      if (isPrimaryKeyPresent(table, primaryKey, inputs.get(primaryKey).toString(), user)) {
        return "Can not insert duplicate entry for primary key";
      }

      FileWriter fw = new FileWriter(tableNameWithPath, true);
      List<String> tableColumns = getColumnNames(table, user);

      if (isTableEmpty) {
        StringBuilder columnName = new StringBuilder();
        for (String col : tableColumns) {
          columnName.append(col).append(",");
        }
        fw.write(columnName.substring(0, columnName.length() - 1) + "\n");
      }

      StringBuilder row = new StringBuilder();
      for (String tableColumn : tableColumns) {
        if (inputs.containsKey(tableColumn)) {
          row.append(inputs.get(tableColumn)).append(",");
        } else {
          row.append(",");
        }
      }
      fw.write(row.substring(0, row.length() - 1) + "\n");
      fw.close();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return "Row inserted";
  }

  public static String deleteProcessor(Map<String, String> deleteMetaMap, User user) throws IOException {
    String returnMessage = "";
    String tableName = deleteMetaMap.get("tableName");

    int compareAt = 100;
    FileReader fr = null;
    try {
      File f = new File(user.getDatabasePath() + "table_" + tableName.trim().toUpperCase() + ".txt");
      File f2 = new File("src/rawDBFiles/Temp/" + "table_" + tableName + ".txt");
      f2.createNewFile();
      fr = new FileReader(f);
      BufferedReader br = new BufferedReader(fr);
      String line = br.readLine();

      FileWriter writerf2 = new FileWriter(f2);
      writerf2.write(line);
      writerf2.write("\n");
      String[] labels = line.split(",");

      for (int i = 0; i < labels.length; i++) {
        if (labels[i].contentEquals(deleteMetaMap.get("conditionColumn"))) {
          compareAt = i;
        }
      }
      String lineFromFile = "";
      while ((lineFromFile = br.readLine()) != null) {
        lineFromFile = lineFromFile.toUpperCase(Locale.ROOT);
        String[] lineBreakDown = lineFromFile.split(",");
        if (deleteMetaMap.get("conditionOperator").equals("=")) {
          if (lineBreakDown[compareAt].contentEquals(deleteMetaMap.get("conditionValue"))) {
          } else {
            writerf2.write(lineFromFile);
            writerf2.write("\n");
          }
        } else if (deleteMetaMap.get("conditionOperator").contentEquals("!=")) {
          if (lineBreakDown[compareAt].contentEquals(deleteMetaMap.get("conditionValue"))) {
            writerf2.write(lineFromFile);
            writerf2.write("\n");
          }
        }
      }
      fr.close();
      boolean renameSuccessful = f2.renameTo(f);
      writerf2.close();
      f.delete();
      Path temp = Files.move
              (Paths.get(f2.getAbsolutePath()),
                      Paths.get(user.getDatabasePath() + "//table_" + tableName + ".txt"));
      returnMessage = "DELETE PROCESS COMPLETED SUCCESSFULLY";
    } catch (IOException e) {
      e.printStackTrace();
    }
    return returnMessage;
  }
}
