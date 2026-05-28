import java.sql.*;
public class TestQuery {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/tenant_sys?user=root&password=root");
        ResultSet rs = c.createStatement().executeQuery("SELECT email, password, active, role_id, user_type FROM users");
        while(rs.next()) {
            System.out.println(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getBoolean(3) + ", " + rs.getLong(4) + ", " + rs.getString(5));
        }
    }
}
