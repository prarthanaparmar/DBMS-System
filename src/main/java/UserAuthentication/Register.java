package UserAuthentication;

import java.io.*;
import java.util.Scanner;

import static Home.HomePage.homePage;

public class Register {
  Scanner sc;
  User user;
  File file;

  public Register(User user) {
    sc = new Scanner(System.in);
    this.user = user;
    file = new File("src/userDetails.txt");
  }

  public User userInput() {
    System.out.println("<================ Registration ================>");
    System.out.print("\nEnter UserName : ");
    String userName = sc.nextLine();
    String password;
    do {
      System.out.print("Enter Password : ");
      password = sc.nextLine();
      if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&()=+])(?=.*\\S+$).{8,20}$")) {
        System.out.println("\n*** Invalid password ***\n" +
                "Must have at least 1 lower & upper letter\n" +
                "Must have one special symbol (!@#$%^&()=+)\n" +
                "Must be 8 to 20 characters\n");
      }
    } while (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&()=+])(?=.*\\S+$).{8,20}$"));

    String confirmPass;
    do {
      System.out.print("Enter Confirm Password : ");
      confirmPass = sc.nextLine();
      if (password.equals(confirmPass)) {
        System.out.println("Please answer the security question properly!!\nWhat is the city of your birth?");
        String answer = sc.next();
        registerUser(userName.trim(), password.trim(), answer.trim(), user);
      } else {
        System.out.println("Both entered password are not same !! Try again");
      }
    } while (!password.equals(confirmPass));
    return user;
  }

  public void registerUser(String userN, String pass, String answer, User user) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;
      boolean isExist = false;
      while ((line = br.readLine()) != null) {
        String[] userPass = line.split("-->");
        if (userPass.length > 1) {
          if (userN.equals(userPass[0].trim())) {
            System.out.println("\n*** Already registered with the system ***\n");
            isExist = true;
            break;
          }
        }
      }
      if (isExist) {
        homePage();
      } else {
        user.setUserName(userN.trim());
        user.setPassword(pass.trim());
        user.setAnswer(answer.trim());
        user.setValid(true);
        FileWriter fw = new FileWriter(file, true);
        fw.write(userN.trim() + "-->" + EncryptionDecryption.encryptString(pass.trim()) + "-->" + EncryptionDecryption.encryptString(answer.trim()) + "\n");
        fw.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}