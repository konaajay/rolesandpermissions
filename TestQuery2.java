import java.sql.*;
public class TestQuery2 {
    public static void main(String[] args) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/tenant_sys?user=root&password=root");
        ResultSet rs = c.createStatement().executeQuery("SELECT email, tenant_id FROM users");
        while(rs.next()) {
            System.out.println(rs.getString(1) + ", tenant_id=" + rs.getLong(2));
        }
    }
}
