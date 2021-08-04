package UserAuthentication;

import Home.HomePage;

import java.io.*;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static Home.HomePage.homePage;

public class Login {
  Scanner sc;
  User user;

  public Login(User user) {
    sc = new Scanner(System.in);
    this.user = user;
  }

  public User userInput() throws IOException {
    System.out.println("<================ Login ================>");
    String userName;
    do {
      System.out.print("\nEnter UserName : ");
      userName = sc.nextLine();
      if (!checkUserExist(userName)) {
        System.out.println("User does not exits!!");
        homePage();
      }
    } while (!checkUserExist(userName));
    System.out.print("Enter Password : ");
    String password = sc.nextLine();
    System.out.println("What is the city of your birth?");
    String answer = sc.next();

    verifyUser(userName.trim(), password.trim(), answer, user);
    return user;
  }

  public void verifyUser(String userN, String pass, String answer, User user) {
    try {
      File file = new File("src/userDetails.txt");
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        String[] userPass = line.split("-->");
        if (userPass.length > 1) {

          if (userN.equals(userPass[0].trim()) &&
                  pass.equals(EncryptionDecryption.decryptString(userPass[1].trim())) &&
                  answer.equals(EncryptionDecryption.decryptString(userPass[2].trim()))) {
            user.setUserName(userN.trim());
            user.setPassword(pass.trim());
            user.setAnswer(answer.trim());
            user.setValid(true);
            break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Boolean checkUserExist(String userN) {
    try {
      File file = new File("src/userDetails.txt");
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      while ((line = br.readLine()) != null) {
        String[] userPass = line.split("-->");
        if (userPass.length > 1) {
          if (userN.equals(userPass[0].trim())) {
            return true;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}