import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class QuickEncryptTest {
    public static void main(String[] args) {
        try {
            String password = "123456";
            String encryptionKey = "SketchNote2025!!"; // Key từ application.yaml
            
            // Encrypt
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
            
            System.out.println("========================================");
            System.out.println("Password: " + password);
            System.out.println("Encrypted: " + encrypted);
            System.out.println("Length: " + encrypted.length() + " characters");
            System.out.println("========================================");
            
            // Decrypt để verify
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            String decrypted = new String(decryptedBytes);
            System.out.println("Decrypted: " + decrypted);
            System.out.println("Match: " + password.equals(decrypted));
            System.out.println("========================================");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
