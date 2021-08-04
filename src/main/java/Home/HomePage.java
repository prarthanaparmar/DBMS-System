package Home;

import ERD.CreateERD;
import SQLDumps.SQLDump;
import Transactions.TransactionManage;
import UserAuthentication.Login;
import UserAuthentication.Register;
import UserAuthentication.User;
import Parser.QueryValidator;

import java.io.IOException;
import java.util.Scanner;

public class HomePage {

  public static void main(String[] args) throws IOException {
    homePage();
  }

  public static void homePage() throws IOException {

    System.out.println("===============================================================");
    System.out.println("\t<== WELCOME TO RELATIONAL DATABASE MANAGEMENT SYSTEM ==>\t");
    System.out.println("===============================================================");
    User user = new User();
    user.setValid(false);
    Scanner choiceInput = new Scanner(System.in);
    System.out.println("1. Login");
    System.out.println("2. Register");
    System.out.println("3. Exit");
    System.out.println("Enter Your choice :");
    String choice1 = choiceInput.nextLine();

    switch (choice1) {
      case "1":
        do {
          Login login = new Login(user);
          user = login.userInput();
          if (user.getValid()) {
            System.out.println("\nLogin Successful !!");
          } else {
            System.out.println("\nInvalid Credentials !!");
            homePage();
          }
        } while (!user.getValid());
        break;
      case "2":
        Register register = new Register(user);
        user = register.userInput();
        if (user.getValid()) {
          System.out.println("\nRegistered Successful !!");
        }
        break;
      case "3":
        System.exit(1);
        break;
    }

    Scanner actionInput = new Scanner(System.in);
    int actionFlag = 0;
    do {
      System.out.println("\nChoose one of the Operations");
      System.out.println("1. Enter Query");
      System.out.println("2. Export SQL Dump");
      System.out.println("3. Generate ERD");
      System.out.println("4. Show Database State");
      System.out.println("5. Transaction Management");
      System.out.println("6. Exit");
      System.out.print("\nEnter your Choice : ");
      String action = actionInput.nextLine();

      if (action.matches("[0-9]+")) {
        int choice = Integer.parseInt(action);
        if (choice > 0 && choice <= 6) {
          switch (choice) {
            case 1:
              QueryValidator.runQuery(user);
              break;
            case 2:
              SQLDump.generateSQLDump(user);
              break;
            case 3:
              CreateERD.generateERD(user);
              break;
            case 4:
              SQLDump.printDatabaseState(user);
              break;
            case 5:
              TransactionManage.manageTransactions(user);
              break;
            case 6:
              actionFlag = 1;
              homePage();
              break;
          }
        } else {
          System.out.println("\nInvalid Input ! Please Select Valid Option.\n");
        }
      } else {
        System.out.println("\nInvalid Input ! Only Numbers are allowed\n");
      }
    } while (actionFlag == 0);
  }
}