import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBcryptHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "password123";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Verify
        String testHash = "$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq9mExSB4EJYM9VqL6h8K4YdLz8Dye";
        System.out.println("\nVerifying test hash matches password123: " + encoder.matches(password, testHash));
    }
}
