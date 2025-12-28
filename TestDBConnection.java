import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDBConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/smd_database";
        String user = "smd_user";
        String password = "smd_password";
        
        System.out.println("Testing database connection...");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        System.out.println("Password: " + password);
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("\n✅ Connection successful!");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT email, full_name FROM core_service.users WHERE email = 'principal@smd.edu.vn'");
            
            if (rs.next()) {
                System.out.println("\n✅ Query successful!");
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Name: " + rs.getString("full_name"));
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ Connection failed!");
            e.printStackTrace();
        }
    }
}
