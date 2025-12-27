import java.util.regex.Pattern;

public class TestBcrypt {
    // BCrypt pattern - starts with $2a$, $2b$, or $2y$ followed by cost factor
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\$2[aby]?\\$\\d{1,2}\\$.{53}");
    
    public static void main(String[] args) {
        String hash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
        System.out.println("Expected length: 60");
        System.out.println("Is BCrypt format: " + BCRYPT_PATTERN.matcher(hash).matches());
        
        // Check for hidden characters
        System.out.println("\nCharacter analysis:");
        for (int i = 0; i < hash.length(); i++) {
            char c = hash.charAt(i);
            if (c < 32 || c > 126) {
                System.out.println("Position " + i + ": non-printable char code=" + (int)c);
            }
        }
        System.out.println("Done checking");
    }
}
