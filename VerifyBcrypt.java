// Run: javac -cp "path/to/spring-security-crypto.jar" VerifyBcrypt.java && java VerifyBcrypt
// Or simply run with the password hash from database

import java.security.SecureRandom;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public class VerifyBcrypt {
    private static final int BCRYPT_SALT_LEN = 16;
    private static final int BCRYPT_MINLOGROUNDS = 4;
    private static final int BCRYPT_MAXLOGROUNDS = 31;

    public static void main(String[] args) {
        String password = "123456";
        String hash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
        System.out.println();
        
        // Check format
        if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
            System.out.println("ERROR: Invalid BCrypt prefix!");
            return;
        }
        
        // Parse cost factor
        int costStart = hash.indexOf('$', 3) + 1;
        String costStr = hash.substring(4, costStart - 1);
        System.out.println("Cost factor: " + costStr);
        
        // The hash should be 60 characters for BCrypt
        if (hash.length() != 60) {
            System.out.println("WARNING: BCrypt hash should be 60 characters!");
        }
        
        // Print byte values to check for BOM or hidden chars
        System.out.println("\nFirst 10 bytes of hash:");
        byte[] bytes = hash.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < Math.min(10, bytes.length); i++) {
            System.out.print((int)bytes[i] + " ");
        }
        System.out.println("\n\nExpected: 36 50 97 36 49 48 36 ..."); // $2a$10$ in ASCII
        
        System.out.println("\nTo verify, use Spring Boot with BCryptPasswordEncoder:");
        System.out.println("encoder.matches(\"123456\", hash) should return true");
    }
}
