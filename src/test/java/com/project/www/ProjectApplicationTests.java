package com.project.www;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class ProjectApplicationTests {

    static {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/rbac_db?createDatabaseIfNotExist=true", "root", "root");
             java.sql.Statement stmt = conn.createStatement()) {
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                java.util.List<String> dbsToDrop = new java.util.ArrayList<>();
                while (rs.next()) {
                    String db = rs.getString(1);
                    if (db.startsWith("tenant_")) {
                        dbsToDrop.add(db);
                    }
                }
                for (String db : dbsToDrop) {
                    stmt.execute("DROP DATABASE IF EXISTS `" + db + "`");
                }
            }
        } catch (Exception e) {
            System.err.println("Static cleanup failed: " + e.getMessage());
        }
    }

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }
}
