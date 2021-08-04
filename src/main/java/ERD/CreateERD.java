package ERD;

import UserAuthentication.User;

import java.util.*;

import static Processor.ManageMetaData.getRawFromMetaData;
import static SQLDumps.SQLDump.isDataExist;

public class CreateERD {

  public static void generateERD(User user) {
    if (user.getDatabasePath() != null) {
      Map<String, Map<String, ArrayList<String>>> rawDataMap = getRawFromMetaData(user);
      Map<String, ArrayList<String>> mapTableReferences = new HashMap<>();
      if (!rawDataMap.keySet().isEmpty()) {
        for (String table : rawDataMap.keySet()) {
          ArrayList<String> foreignKeys = new ArrayList<>(rawDataMap.get(table).get("foreignKey"));
          mapTableReferences.put(table, foreignKeys);
        }
        ArrayList<String> allReferenceTables = new ArrayList<>();
        for (String table : mapTableReferences.keySet()) {
          for (String ref : mapTableReferences.get(table)) {
            allReferenceTables.add(ref.split(":")[1]);
          }
        }

        System.out.println("\n<================ Entity Relation Diagram ================>\n");
        for (String table : mapTableReferences.keySet()) {
          if (!allReferenceTables.contains(table) && mapTableReferences.get(table).isEmpty()) {
            System.out.println(table);
          }
        }
        for (String table : mapTableReferences.keySet()) {
          String relationName = "1:1";
          if (!isDataExist(table, user).isEmpty()) {
            relationName = "1:M";
          }
          if (!mapTableReferences.get(table).isEmpty()) {
            for (String rfTable : mapTableReferences.get(table)) {
              System.out.println(table + " (" + rfTable.split(":")[0] + ") -- " + relationName + " -- " + rfTable.split(":")[1] + " (" + rfTable.split(":")[2] + ")");
            }
          }
        }
      } else {
        System.out.println("No Tables Created yet !!!");
      }

    } else {
      System.out.println("Please Select Database First !!!");
    }
  }
}
