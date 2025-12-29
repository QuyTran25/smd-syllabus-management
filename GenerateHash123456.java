import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash123456 {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("123456");
        System.out.println("BCrypt hash for '123456': " + hash);
        System.out.println("Verify: " + encoder.matches("123456", hash));
        
        // Test the old hash
        String oldHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        System.out.println("Old hash matches '123456': " + encoder.matches("123456", oldHash));
        System.out.println("Old hash matches 'password': " + encoder.matches("password", oldHash));
    }
}
