import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        String password = "123456";
        String existingHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        
        System.out.println("=== BCrypt Password Test ===");
        System.out.println("Password: " + password);
        System.out.println();
        
        System.out.println("Existing hash from DB:");
        System.out.println(existingHash);
        System.out.println("Length: " + existingHash.length());
        System.out.println();
        
        System.out.println("Testing if existing hash matches password:");
        boolean matches = encoder.matches(password, existingHash);
        System.out.println("Match result: " + matches);
        System.out.println();
        
        System.out.println("Generating NEW BCrypt hash:");
        String newHash = encoder.encode(password);
        System.out.println(newHash);
        System.out.println("Length: " + newHash.length());
        System.out.println();
        
        System.out.println("Testing if new hash matches password:");
        boolean newMatches = encoder.matches(password, newHash);
        System.out.println("Match result: " + newMatches);
        System.out.println();
        
        System.out.println("=== SQL Update Statement ===");
        System.out.println("UPDATE core_service.users SET password_hash = '" + newHash + "' WHERE status = 'ACTIVE';");
    }
}
