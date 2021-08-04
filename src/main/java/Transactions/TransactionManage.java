package Transactions;

import Parser.QueryValidator;
import UserAuthentication.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TransactionManage {

  static Scanner scanner = new Scanner(System.in);
  static final String INVALID_QUERY = "Invalid Query !!!";

  public static void manageTransactions(User user) throws IOException {
    System.out.println("\nSTART TRANSACTION");
    ArrayList<String> queryList = new ArrayList<>();
    boolean flag = true;
    do {
      String query = QueryValidator.inputQuery();
      String qType = QueryValidator.getQueryType(query);
      if (!qType.equals(INVALID_QUERY)) {
        queryList.add(query);
      }
      System.out.println("Want to add query in this transaction ? (yes/ any other char)");
      String ans = scanner.nextLine();
      if (!ans.equalsIgnoreCase("yes")) {
        flag = false;
      }
    } while (flag);
    System.out.println("END TRANSACTION");
    System.out.println("\n Transaction Logs");
    if (!queryList.isEmpty()) {
      for (String que : queryList) {
        String message = QueryValidator.checkForParser(que, QueryValidator.getQueryType(que), user);
        System.out.println(message);
      }
    }
  }
}
