package com.sketchnotes.identityservice;

import com.sketchnotes.identityservice.utils.PasswordEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PasswordEncryptionTest {

    @Autowired
    private PasswordEncryptionUtil passwordEncryptionUtil;

    @Test
    public void testEncryptPassword() {
        String password = "123456";
        
        // Encrypt
        String encrypted = passwordEncryptionUtil.encrypt(password);
        System.out.println("===========================================");
        System.out.println("Original Password: " + password);
        System.out.println("Encrypted Password: " + encrypted);
        System.out.println("Encrypted Length: " + encrypted.length());
        
        // Decrypt to verify
        String decrypted = passwordEncryptionUtil.decrypt(encrypted);
        System.out.println("Decrypted Password: " + decrypted);
        System.out.println("===========================================");
        
        // Verify
        assert password.equals(decrypted) : "Decryption failed!";
        System.out.println("✅ Encryption/Decryption successful!");
    }
    
    @Test
    public void testMultiplePasswords() {
        String[] passwords = {"123456", "MyPassword123", "Google@2024", "abc123"};
        
        System.out.println("\n===========================================");
        System.out.println("Testing multiple passwords:");
        System.out.println("===========================================");
        
        for (String password : passwords) {
            String encrypted = passwordEncryptionUtil.encrypt(password);
            String decrypted = passwordEncryptionUtil.decrypt(encrypted);
            
            System.out.println("\nPassword: " + password);
            System.out.println("Encrypted: " + encrypted);
            System.out.println("Decrypted: " + decrypted);
            System.out.println("Match: " + password.equals(decrypted));
        }
        System.out.println("===========================================");
    }
}
