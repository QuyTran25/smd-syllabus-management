import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcryptDirect {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String hash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        String password = "123456";
        
        System.out.println("Testing password: " + password);
        System.out.println("Against hash: " + hash);
        System.out.println("Match: " + encoder.matches(password, hash));
        
        // Generate new hash for 123456
        String newHash = encoder.encode(password);
        System.out.println("\nNew hash for 123456: " + newHash);
        System.out.println("Verify new hash: " + encoder.matches(password, newHash));
    }
}
