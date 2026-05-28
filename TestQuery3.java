import java.sql.*;
public class TestQuery3 {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/rbac_db?user=root&password=root");
        ResultSet rs = c.createStatement().executeQuery("SELECT id, code, active FROM tenants");
        while(rs.next()) {
            System.out.println("ID: " + rs.getLong(1) + ", Code: " + rs.getString(2) + ", Active: " + rs.getBoolean(3));
        }
    }
}
