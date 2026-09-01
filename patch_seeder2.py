with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\config\DatabaseSeeder.java", "r", encoding="utf-8") as f:
    content = f.read()

patch_code = """
                            // Asset Lifecycle Patch
                            addColumnIfNotExists(tConn, t.getDbName(), "requirement_items", "item_type", "VARCHAR(20) DEFAULT 'ASSET'");
                            addColumnIfNotExists(tConn, t.getDbName(), "product_assignments", "status", "VARCHAR(50) DEFAULT 'ASSIGNED'");
                            addColumnIfNotExists(tConn, t.getDbName(), "product_assignments", "asset_identifier", "VARCHAR(100) DEFAULT NULL");
                            addColumnIfNotExists(tConn, t.getDbName(), "product_assignments", "replaced_by_assignment_id", "BIGINT DEFAULT NULL");
                            
                            tStmt.executeUpdate("CREATE TABLE IF NOT EXISTS product_lifecycle_events (id BIGINT AUTO_INCREMENT PRIMARY KEY, tenant_id BIGINT NOT NULL, assignment_id BIGINT NOT NULL, event_type VARCHAR(50) NOT NULL, previous_status VARCHAR(50), new_status VARCHAR(50) NOT NULL, performed_by BIGINT NOT NULL, assigned_to BIGINT, description TEXT, created_at DATETIME NOT NULL, FOREIGN KEY (assignment_id) REFERENCES product_assignments(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
"""

# Find the SECOND occurrence of '// Patch roles' or use a more specific string
target = 'addColumnIfNotExists(tConn, t.getDbName(), "roles", "show_in_user_form",'
content = content.replace(target, patch_code + "\n                            " + target)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\config\DatabaseSeeder.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Updated DatabaseSeeder safely")
