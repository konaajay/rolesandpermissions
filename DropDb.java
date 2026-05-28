import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropDb {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=root");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP DATABASE IF EXISTS rbac_db");
            stmt.executeUpdate("DROP DATABASE IF EXISTS tenant_sys");
            stmt.executeUpdate("DROP DATABASE IF EXISTS tenant_hrms");
            stmt.executeUpdate("DROP DATABASE IF EXISTS tenant_a"); // just in case
            System.out.println("Databases dropped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
