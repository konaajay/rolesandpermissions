package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rbac_db", "root", "root");
            Statement stmt = conn.createStatement();
            
            // 1. Check roles
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM roles WHERE name='SUPER_ADMIN'");
            int roleId = -1;
            if (rs.next()) {
                roleId = rs.getInt("id");
                System.out.println("SUPER_ADMIN Role ID: " + roleId);
            }
            
            // 2. Check role permissions
            if (roleId != -1) {
                rs = stmt.executeQuery("SELECT count(*) AS c FROM role_permissions WHERE role_id=" + roleId);
                if (rs.next()) {
                    System.out.println("Permissions count for SUPER_ADMIN: " + rs.getInt("c"));
                }
            }
            
            // 3. Check users
            rs = stmt.executeQuery("SELECT id, email, role_id FROM users WHERE email='superadmin@system.com'");
            if (rs.next()) {
                System.out.println("User superadmin@system.com exists, role_id: " + rs.getInt("role_id"));
            }
            
            // 4. Check USER_VIEW permission
            rs = stmt.executeQuery("SELECT id, module, action FROM permissions WHERE permission_key='USER_VIEW'");
            if (rs.next()) {
                System.out.println("USER_VIEW permission exists with ID: " + rs.getInt("id"));
            } else {
                System.out.println("USER_VIEW permission DOES NOT EXIST!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
