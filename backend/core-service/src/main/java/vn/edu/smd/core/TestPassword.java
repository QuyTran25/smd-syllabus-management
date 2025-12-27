package vn.edu.smd.core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "123456";
        String storedHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        
        // Test verify
        boolean matches = encoder.matches(rawPassword, storedHash);
        System.out.println("Password: " + rawPassword);
        System.out.println("Stored Hash: " + storedHash);
        System.out.println("Matches: " + matches);
        
        // Generate new hash
        String newHash = encoder.encode(rawPassword);
        System.out.println("\nNew Hash for 123456: " + newHash);
        System.out.println("Verify new hash: " + encoder.matches(rawPassword, newHash));
    }
}
