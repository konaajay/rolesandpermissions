package com.project.www;

import java.sql.*;

public class FixDb {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tenant_hrm", "root", "root");
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("DELETE FROM role_permissions WHERE role_id = 1");
            for(int i=1; i<=10; i++) {
                stmt.executeUpdate("INSERT INTO role_permissions (role_id, permission_id) VALUES (1, " + i + ")");
            }
            
            stmt.executeUpdate("DELETE FROM role_permissions WHERE role_id = 3");
            stmt.executeUpdate("INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 1), (3, 2), (3, 3), (3, 4)");
            
            System.out.println("Permissions successfully restored!");
        }
    }
}
