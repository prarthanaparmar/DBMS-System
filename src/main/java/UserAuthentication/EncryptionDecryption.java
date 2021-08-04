package UserAuthentication;

public class EncryptionDecryption {

  public static String encryptString(String toEncrypt) {
    int shift = 2;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < toEncrypt.length(); i++) {
      if (Character.isUpperCase(toEncrypt.charAt(i))) {
        char ch = (char) (((int) toEncrypt.charAt(i) + shift - 65) % 26 + 65);
        result.append(ch);
      } else if (Character.isLowerCase(toEncrypt.charAt(i))) {
        char ch = (char) (((int) toEncrypt.charAt(i) + shift - 97) % 26 + 97);
        result.append(ch);
      } else {
        char ch = (char) (((int) toEncrypt.charAt(i) + shift));
        result.append(ch);
      }
    }
    return result.toString();
  }

  public static String decryptString(String toDecrypt) {
    int shift = 2;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < toDecrypt.length(); i++) {
      if (Character.isUpperCase(toDecrypt.charAt(i))) {
        char ch = (char) (((int) toDecrypt.charAt(i) - shift - 65) % 26 + 65);
        result.append(ch);
      } else if (Character.isLowerCase(toDecrypt.charAt(i))) {
        char ch = (char) (((int) toDecrypt.charAt(i) - shift - 97) % 26 + 97);
        result.append(ch);
      } else {
        char ch = (char) (((int) toDecrypt.charAt(i) - shift));
        result.append(ch);
      }
    }
    return result.toString();
  }
}
