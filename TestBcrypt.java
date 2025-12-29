import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hashInDb = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        
        System.out.println("Testing password: " + password);
        System.out.println("Hash in DB: " + hashInDb);
        System.out.println("Hash length: " + hashInDb.length());
        System.out.println("Matches: " + encoder.matches(password, hashInDb));
        
        // Generate new hash
        String newHash = encoder.encode(password);
        System.out.println("\nNew generated hash: " + newHash);
        System.out.println("New hash length: " + newHash.length());
        System.out.println("New hash matches: " + encoder.matches(password, newHash));
    }
}
