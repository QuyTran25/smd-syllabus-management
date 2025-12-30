package vn.edu.smd.core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "vn.edu.smd.core")
@EnableJpaRepositories(basePackages = "vn.edu.smd.core.repository")
@EntityScan(basePackages = "vn.edu.smd.core.entity")
public class TestPasswordApp {
    public static void main(String[] args) {
        // Just test BCrypt without starting Spring
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String hash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        String password = "123456";
        
        System.out.println("=== BCRYPT TEST ===");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Match: " + encoder.matches(password, hash));
        
        // Generate new hash
        String newHash = encoder.encode(password);
        System.out.println("\nNew hash: " + newHash);
        System.out.println("Verify new: " + encoder.matches(password, newHash));
    }
}
