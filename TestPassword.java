import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String hash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        
        System.out.println("Testing password: 123456");
        System.out.println("Match: " + encoder.matches("123456", hash));
        
        System.out.println("\nTesting password: password123");
        System.out.println("Match: " + encoder.matches("password123", hash));
        
        System.out.println("\nTesting password: password");
        System.out.println("Match: " + encoder.matches("password", hash));
        
        // Generate new hash for password123
        System.out.println("\n--- New BCrypt hashes ---");
        System.out.println("Hash for 'password123': " + encoder.encode("password123"));
        System.out.println("Hash for 'password': " + encoder.encode("password"));
        System.out.println("Hash for '123456': " + encoder.encode("123456"));
    }
}
