package vn.edu.smd.core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hashes
        String hash123 = encoder.encode("password123");
        String hashPwd = encoder.encode("password");
        
        System.out.println("=== COPY THESE SQL COMMANDS ===");
        System.out.println();
        System.out.println("-- For principal (password123):");
        System.out.println("UPDATE core_service.users SET password_hash = '" + hash123 + "' WHERE email = 'principal@smd.edu.vn';");
        System.out.println();
        System.out.println("-- For student (password):");
        System.out.println("UPDATE core_service.users SET password_hash = '" + hashPwd + "' WHERE email = 'student@smd.edu.vn';");
        System.out.println();
        System.out.println("-- Verify:");
        System.out.println("SELECT email, LEFT(password_hash, 30) FROM core_service.users WHERE email IN ('principal@smd.edu.vn', 'student@smd.edu.vn');");
    }
}
