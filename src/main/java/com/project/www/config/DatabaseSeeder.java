package com.project.www.config;

import com.project.www.entity.*;
import com.project.www.repository.*;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RoleExtraFieldRepository roleExtraFieldRepository;
    private final RoleHierarchyRepository roleHierarchyRepository;
    private final UserReportingRepository userReportingRepository;
    private final UserRepository userRepository;
    private final TenantSequenceRepository tenantSequenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSourceConfig dataSourceConfig;
    private final DataSource routingDataSource;
    private final DataSource masterDataSource;
    private final PipelineStageRepository pipelineStageRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database seeding check...");

        try (java.sql.Connection conn = masterDataSource.getConnection()) {
            ResourceDatabasePopulator masterPopulator = new ResourceDatabasePopulator();
            masterPopulator.addScript(new ClassPathResource("master-schema.sql"));
            masterPopulator.execute(masterDataSource);
            log.info("Successfully executed master-schema.sql on rbac_db");

            // Patch existing tenant_modules in master database to add missing columns
            try (java.sql.Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN amount DOUBLE");
                    log.info("Patched amount for tenant_modules");
                } catch (Exception e) {}
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN payment_method VARCHAR(255)");
                    log.info("Patched payment_method for tenant_modules");
                } catch (Exception e) {}
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN special_requirements TEXT");
                    log.info("Patched special_requirements for tenant_modules");
                } catch (Exception e) {}
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN extra_charges DOUBLE");
                    log.info("Patched extra_charges for tenant_modules");
                } catch (Exception e) {}
            } catch (Exception e) {
                log.warn("Failed to patch tenant_modules: " + e.getMessage());
            }
            
            // Patch existing tenant databases to add missing columns to tenant_settings
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%'")) {
                java.util.List<String> dbs = new java.util.ArrayList<>();
                while (rs.next()) {
                    dbs.add(rs.getString(1));
                }
                for (String db : dbs) {
                    try {
                        stmt.executeUpdate("ALTER TABLE `" + db + "`.tenant_settings ADD COLUMN employee_sequence BIGINT DEFAULT 0");
                        log.info("Patched employee_sequence for " + db);
                    } catch (Exception e) {}
                    try {
                        stmt.executeUpdate("ALTER TABLE `" + db + "`.tenant_settings ADD COLUMN lead_sequence BIGINT DEFAULT 0");
                        log.info("Patched lead_sequence for " + db);
                    } catch (Exception e) {}
                    try {
                        stmt.executeUpdate("ALTER TABLE `" + db + "`.tenant_settings ADD COLUMN lead_id_format VARCHAR(255)");
                        log.info("Patched lead_id_format for " + db);
                    } catch (Exception e) {}
                    try {
                        stmt.executeUpdate("ALTER TABLE `" + db + "`.users ADD COLUMN lead_id VARCHAR(255)");
                        log.info("Patched lead_id for users table in " + db);
                    } catch (Exception e) {}
                    try {
                        stmt.executeUpdate("RENAME TABLE `" + db + "`.student_profiles TO `" + db + "`.lead_profiles");
                        log.info("Renamed student_profiles to lead_profiles in " + db);
                    } catch (Exception e) {}
                    try {
                        stmt.executeUpdate("ALTER TABLE `" + db + "`.lead_profiles CHANGE COLUMN student_id lead_id VARCHAR(255) NOT NULL");
                        log.info("Renamed student_id to lead_id in lead_profiles in " + db);
                    } catch (Exception e) {}
                    try {
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + db + "`.office_locations (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "tenant_id BIGINT NOT NULL, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "latitude DECIMAL(10, 7) NOT NULL, " +
                            "longitude DECIMAL(10, 7) NOT NULL, " +
                            "radius_meters DOUBLE NOT NULL DEFAULT 30.0, " +
                            "tracking_interval_sec INT NOT NULL DEFAULT 300, " +
                            "max_accuracy_meters INT NOT NULL DEFAULT 100, " +
                            "max_idle_minutes INT NOT NULL DEFAULT 30, " +
                            "created_at DATETIME NOT NULL, " +
                            "updated_at DATETIME, " +
                            "KEY idx_office_location_name (name), " +
                            "KEY idx_office_location_tenant (tenant_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        log.info("Created office_locations table in " + db);
                    } catch (Exception e) {
                        log.warn("Error creating office_locations in " + db + ": " + e.getMessage());
                    }
                    try {
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + db + "`.attendance_shifts (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "start_time TIME NOT NULL, " +
                            "end_time TIME NOT NULL, " +
                            "grace_minutes INT NOT NULL DEFAULT 15, " +
                            "min_half_day_minutes INT NOT NULL DEFAULT 240, " +
                            "min_full_day_minutes INT NOT NULL DEFAULT 480, " +
                            "short_break_start_time TIME, " +
                            "short_break_end_time TIME, " +
                            "long_break_start_time TIME, " +
                            "long_break_end_time TIME, " +
                            "office_id BIGINT NOT NULL, " +
                            "tenant_id BIGINT NOT NULL, " +
                            "created_at DATETIME NOT NULL, " +
                            "updated_at DATETIME, " +
                            "FOREIGN KEY (office_id) REFERENCES `" + db + "`.office_locations(id) ON DELETE CASCADE, " +
                            "KEY idx_shift_name (name), " +
                            "KEY idx_shift_tenant (tenant_id), " +
                            "KEY idx_shift_office (office_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        log.info("Created attendance_shifts table in " + db);
                    } catch (Exception e) {
                        log.warn("Error creating attendance_shifts in " + db + ": " + e.getMessage());
                    }
                    try {
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + db + "`.pipeline_stages (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "tenant_id BIGINT NOT NULL, " +
                            "status_value VARCHAR(255) NOT NULL, " +
                            "label VARCHAR(255) NOT NULL, " +
                            "color VARCHAR(50), " +
                            "analytic_bucket VARCHAR(255), " +
                            "order_index INT NOT NULL, " +
                            "active BOOLEAN NOT NULL DEFAULT TRUE, " +

                            "require_note BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "require_date BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "create_task BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "created_at DATETIME NOT NULL, " +
                            "updated_at DATETIME, " +
                            "created_by VARCHAR(255), " +
                            "updated_by VARCHAR(255), " +
                            "KEY idx_pipeline_tenant (tenant_id), " +
                            "KEY idx_pipeline_order (tenant_id, order_index)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        log.info("Created pipeline_stages table in " + db);
                    } catch (Exception e) {
                        log.warn("Error creating pipeline_stages in " + db + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to patch databases: " + e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to execute master-schema.sql: " + e.getMessage());
        }

        // Ensure "System" tenant exists in Master DB
        Tenant systemTenant = tenantRepository.findByName("System")
                .orElseGet(() -> {
                    log.info("System tenant not found in master database. Creating System tenant record...");
                    Tenant t = Tenant.builder()
                            .name("System")
                            .code("SYS")
                            .dbName("tenant_sys")
                            .active(true)
                            .build();
                    t = tenantRepository.save(t);

                    // Create tenant_sys database if not exists
                    try (java.sql.Connection conn = masterDataSource.getConnection();
                            java.sql.Statement stmt = conn.createStatement()) {
                        stmt.execute("CREATE DATABASE IF NOT EXISTS tenant_sys");
                        log.info("Created tenant_sys database");
                    } catch (Exception e) {
                        log.warn("Failed to create tenant_sys database: " + e.getMessage());
                    }

                    // Run schema.sql on tenant_sys
                    DataSource tenantSysDs = dataSourceConfig.createTenantDataSource("tenant_sys", null, null);
                    try {
                        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                        populator.addScript(new ClassPathResource("schema.sql"));
                        populator.execute(tenantSysDs);
                        log.info("Executed schema.sql on tenant_sys");
                    } catch (Exception e) {
                        log.warn("Failed to execute schema.sql on tenant_sys: " + e.getMessage());
                    }

                    return t;
                });

        final Long systemTenantId = systemTenant.getId();
        final String systemTenantCode = systemTenant.getCode();
        final String dbName = "tenant_sys";

        // Seed System Modules
        String[] allModules = {
                com.project.www.constants.Modules.LEAD,
                com.project.www.constants.Modules.EMPLOYEE,
                com.project.www.constants.Modules.COURSE,
                com.project.www.constants.Modules.AFFILIATE,
                com.project.www.constants.Modules.MARKETING,
                com.project.www.constants.Modules.CRM,
                com.project.www.constants.Modules.HRMS,
                com.project.www.constants.Modules.PAYROLL,
                com.project.www.constants.Modules.ATTENDANCE,
                com.project.www.constants.Modules.LMS,
                com.project.www.constants.Modules.ADMIN
        };
        for (String mod : allModules) {
            if (!tenantModuleRepository.existsByTenantIdAndModuleNameAndActiveTrue(systemTenantId, mod)) {
                tenantModuleRepository.save(TenantModule.builder()
                        .tenantId(systemTenantId)
                        .moduleName(mod)
                        .active(true)
                        .build());
            }
        }

        // 1. Create tenant_sys database if it doesn't exist
        try (Connection connection = masterDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        } catch (Exception e) {
            log.error("Failed to create System tenant database: {}", e.getMessage());
            throw e;
        }

        // 2. Build or resolve tenant_sys datasource and execute schema script
        TenantRoutingDataSource rds = (TenantRoutingDataSource) routingDataSource;
        DataSource tenantDs;
        if (rds.containsDataSource(systemTenantCode)) {
            tenantDs = rds.getDataSource(systemTenantCode);
        } else {
            tenantDs = dataSourceConfig.createTenantDataSource(dbName, null, null);
            rds.addDataSource(systemTenantCode, tenantDs);
        }

        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.execute(tenantDs);
        } catch (Exception e) {
            log.error("Failed to initialize System database schema: {}", e.getMessage());
            throw e;
        }

        // 4. Switch context to System Tenant and seed roles/permissions/user in
        // tenant_sys
        try {
            TenantContext.setCurrentTenant(systemTenantId);
            TenantContext.setCurrentTenantCode(systemTenantCode);

            // Define all required permissions for system / super-admin
            java.util.List<String[]> requiredPermsInfo = java.util.Arrays.asList(
                    new String[] { "TENANT", "CREATE", "Ability to onboard new tenants" },
                    new String[] { "TENANT", "VIEW", "Ability to view tenants" },
                    new String[] { "TENANT", "ENABLE", "Ability to enable tenants" },
                    new String[] { "TENANT", "DISABLE", "Ability to disable tenants" },
                    new String[] { "USER", "CREATE", "Ability to create new users" },
                    new String[] { "USER", "VIEW", "Ability to view users" },
                    new String[] { "USER", "UPDATE", "Ability to update users" },
                    new String[] { "USER", "DELETE", "Ability to delete users" },
                    new String[] { "ROLE", "CREATE", "Ability to create new roles" },
                    new String[] { "ROLE", "UPDATE", "Ability to update roles" },
                    new String[] { "ROLE", "DISABLE", "Ability to disable roles" },
                    new String[] { "ROLE", "ENABLE", "Ability to enable roles" },
                    new String[] { "PERMISSION", "CREATE", "Ability to create permissions" },
                    new String[] { "PERMISSION", "ENABLE", "Ability to enable permissions" },
                    new String[] { "PERMISSION", "DISABLE", "Ability to disable permissions" },
                    // Dummy Module View Permissions for testing UI
                    new String[] { "LEAD", "VIEW", "Ability to view Leads" },
                    new String[] { "COURSE", "VIEW", "Ability to view Courses" },
                    new String[] { "EMPLOYEE", "VIEW", "Ability to view Employees" },
                    new String[] { "HRMS", "VIEW", "Ability to view HRMS" },
                    new String[] { "PAYROLL", "VIEW", "Ability to view Payroll" },
                    new String[] { "ATTENDANCE", "VIEW", "Ability to view Attendance" },
                    new String[] { "LMS", "VIEW", "Ability to view LMS" },
                    new String[] { "AFFILIATE", "VIEW", "Ability to view Affiliate" },
                    new String[] { "MARKETING", "VIEW", "Ability to view Marketing" },
                    new String[] { "CRM", "VIEW", "Ability to view CRM" });

            java.util.List<Permission> existingPerms = permissionRepository.findAllByTenantId(systemTenantId);
            java.util.Map<String, Permission> permMap = existingPerms.stream()
                    .collect(java.util.stream.Collectors.toMap(Permission::getPermissionKey,
                            java.util.function.Function.identity()));

            Set<Permission> superAdminPermissions = new HashSet<>();
            for (String[] permInfo : requiredPermsInfo) {
                String key = (permInfo[0] + "_" + permInfo[1]).toUpperCase();
                Permission perm = permMap.get(key);
                if (perm == null) {
                    perm = Permission.builder()
                            .tenantId(systemTenantId)
                            .module(permInfo[0])
                            .action(permInfo[1])
                            .description(permInfo[2])
                            .active(true)
                            .build();
                    perm = permissionRepository.save(perm);
                    permMap.put(perm.getPermissionKey(), perm);
                }
                superAdminPermissions.add(perm);
            }

            Role superAdminRole = roleRepository.findByNameAndTenantId("SUPER_ADMIN", systemTenantId)
                    .orElseGet(() -> Role.builder()
                            .tenantId(systemTenantId)
                            .name("SUPER_ADMIN")
                            .code("SUPER_ADMIN")
                            .description("Global system administrator")
                            .active(true)
                            .build());
            superAdminRole.setPermissions(superAdminPermissions);
            superAdminRole = roleRepository.save(superAdminRole);

            // Seed LEAD, TEACHER, EMPLOYEE roles and fields for demonstration/testing
            seedRoleAndFields(systemTenantId, "LEAD", "Lead", "Lead role", java.util.Arrays.asList(
                    new FieldSeed("rollNo", "Roll Number", "TEXT", true, null, 1),
                    new FieldSeed("course", "Course", "DROPDOWN", false, "[\"CSE\",\"ECE\",\"ME\",\"CE\"]", 2)
            ));
            seedRoleAndFields(systemTenantId, "TEACHER", "Teacher", "Teacher role", java.util.Arrays.asList(
                    new FieldSeed("subject", "Subject", "TEXT", true, null, 1)
            ));
            seedRoleAndFields(systemTenantId, "EMPLOYEE", "Employee", "Employee role", java.util.Arrays.asList(
                    new FieldSeed("employeeId", "Employee ID", "TEXT", true, null, 1)
            ));

            // Seed BDAN, TEAM_LEAD, MANAGER roles and fields
            seedRoleAndFields(systemTenantId, "BDAN", "BDAN", "Business Development Associate", java.util.Collections.emptyList());
            seedRoleAndFields(systemTenantId, "TEAM_LEAD", "Team Lead", "Team Leader", java.util.Collections.emptyList());
            seedRoleAndFields(systemTenantId, "MANAGER", "Manager", "Manager role", java.util.Collections.emptyList());

            // Seed Role Hierarchies
            seedRoleHierarchy(systemTenantId, "BDAN", "TEAM_LEAD");
            seedRoleHierarchy(systemTenantId, "TEAM_LEAD", "MANAGER");
            seedRoleHierarchy(systemTenantId, "MANAGER", "SUPER_ADMIN");

            // Ensure sequence exists
            int currentYear = java.time.LocalDate.now().getYear();
            if (!tenantSequenceRepository.findByTenantIdAndYear(systemTenantId, currentYear).isPresent()) {
                TenantSequence sequence = TenantSequence.builder()
                        .tenantId(systemTenantId)
                        .year(currentYear)
                        .currentSequence(1L)
                        .build();
                tenantSequenceRepository.save(sequence);
            }

            if (!userRepository.existsByEmailAndTenantId("superadmin@system.com", systemTenantId)) {
                User superAdmin = User.builder()
                        .tenantId(systemTenantId)
                        .firstName("Super")
                        .lastName("Admin")
                        .email("superadmin@system.com")
                        .password(passwordEncoder.encode("superadmin"))
                        .active(true)
                        .role(superAdminRole)
                        .build();
                userRepository.save(superAdmin);
                log.info(
                        "System seeding completed. Super Admin user 'superadmin@system.com' created under Tenant ID: {}",
                        systemTenantId);
            } else {
                User superAdmin = userRepository.findByEmailAndTenantId("superadmin@system.com", systemTenantId).get();
                superAdmin.setRole(superAdminRole);
                userRepository.save(superAdmin);
                log.info(
                        "System seeding updated. Super Admin user 'superadmin@system.com' mapped to updated SUPER_ADMIN role.");
            }

            // Seed reporting hierarchy users
            Role managerRole = roleRepository.findByCodeAndTenantId("MANAGER", systemTenantId).get();
            Role teamLeadRole = roleRepository.findByCodeAndTenantId("TEAM_LEAD", systemTenantId).get();
            Role bdanRole = roleRepository.findByCodeAndTenantId("BDAN", systemTenantId).get();

            User manager = null;
            if (!userRepository.existsByEmailAndTenantId("manager@system.com", systemTenantId)) {
                manager = User.builder()
                        .tenantId(systemTenantId)
                        .firstName("Manager")
                        .lastName("One")
                        .email("manager@system.com")
                        .password(passwordEncoder.encode("manager"))
                        .active(true)
                        .role(managerRole)
                        .build();
                manager = userRepository.save(manager);
                
                // Set reports to Super Admin
                userReportingRepository.save(UserReporting.builder()
                        .tenantId(systemTenantId)
                        .user(manager)
                        .supervisorUser(userRepository.findByEmailAndTenantId("superadmin@system.com", systemTenantId).get())
                        .build());
            } else {
                manager = userRepository.findByEmailAndTenantId("manager@system.com", systemTenantId).get();
            }

            User teamLead = null;
            if (!userRepository.existsByEmailAndTenantId("teamlead@system.com", systemTenantId)) {
                teamLead = User.builder()
                        .tenantId(systemTenantId)
                        .firstName("Team")
                        .lastName("Lead")
                        .email("teamlead@system.com")
                        .password(passwordEncoder.encode("teamlead"))
                        .active(true)
                        .role(teamLeadRole)
                        .build();
                teamLead = userRepository.save(teamLead);

                // Set reports to Manager
                userReportingRepository.save(UserReporting.builder()
                        .tenantId(systemTenantId)
                        .user(teamLead)
                        .supervisorUser(manager)
                        .build());
            } else {
                teamLead = userRepository.findByEmailAndTenantId("teamlead@system.com", systemTenantId).get();
            }

            if (!userRepository.existsByEmailAndTenantId("bdan@system.com", systemTenantId)) {
                User bdan = User.builder()
                        .tenantId(systemTenantId)
                        .firstName("BDAN")
                        .lastName("User")
                        .email("bdan@system.com")
                        .password(passwordEncoder.encode("bdan"))
                        .active(true)
                        .role(bdanRole)
                        .build();
                bdan = userRepository.save(bdan);

                // Set reports to Team Lead
                userReportingRepository.save(UserReporting.builder()
                        .tenantId(systemTenantId)
                        .user(bdan)
                        .supervisorUser(teamLead)
                        .build());
            }

            // Seed default pipeline stages for system tenant
            seedDefaultPipelineStages(systemTenantId);
        } finally {
            TenantContext.clear();
        }
    }

    private void seedDefaultPipelineStages(Long tenantId) {
        if (pipelineStageRepository.findAllByTenantIdOrderByOrderIndexAsc(tenantId).isEmpty()) {
            log.info("Seeding default pipeline stages for tenant: " + tenantId);
            pipelineStageRepository.save(PipelineStage.builder()
                    .tenantId(tenantId)
                    .statusValue("NEW")
                    .label("New Lead")
                    .color("#3b82f6")
                    .analyticBucket("UNASSIGNED")
                    .orderIndex(1)
                    .active(true)

                    .build());
            pipelineStageRepository.save(PipelineStage.builder()
                    .tenantId(tenantId)
                    .statusValue("CONTACTED")
                    .label("Contacted")
                    .color("#f59e0b")
                    .analyticBucket("ENGAGED")
                    .orderIndex(2)
                    .active(true)

                    .build());
            pipelineStageRepository.save(PipelineStage.builder()
                    .tenantId(tenantId)
                    .statusValue("INTERESTED")
                    .label("Interested")
                    .color("#10b981")
                    .analyticBucket("ENGAGED")
                    .orderIndex(3)
                    .active(true)

                    .build());
            pipelineStageRepository.save(PipelineStage.builder()
                    .tenantId(tenantId)
                    .statusValue("UNDER_REVIEW")
                    .label("Under Review")
                    .color("#8b5cf6")
                    .analyticBucket("ENGAGED")
                    .orderIndex(4)
                    .active(true)

                    .build());
            pipelineStageRepository.save(PipelineStage.builder()
                    .tenantId(tenantId)
                    .statusValue("CONVERTED")
                    .label("Converted")
                    .color("#10b981")
                    .analyticBucket("WON")
                    .orderIndex(5)
                    .active(true)

                    .build());
            pipelineStageRepository.save(PipelineStage.builder()
                    .tenantId(tenantId)
                    .statusValue("LOST")
                    .label("Lost")
                    .color("#ef4444")
                    .analyticBucket("LOST")
                    .orderIndex(6)
                    .active(true)

                    .build());
        }
    }

    private static class FieldSeed {
        String name;
        String label;
        String type;
        boolean required;
        String optionsJson;
        int order;

        FieldSeed(String name, String label, String type, boolean required, String optionsJson, int order) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.required = required;
            this.optionsJson = optionsJson;
            this.order = order;
        }
    }

    private void seedRoleAndFields(Long tenantId, String code, String name, String desc, java.util.List<FieldSeed> fields) {
        Role role = roleRepository.findByCodeAndTenantId(code, tenantId)
                .or(() -> roleRepository.findByNameAndTenantId(code, tenantId))
                .orElseGet(() -> {
                    Role r = Role.builder()
                            .tenantId(tenantId)
                            .name(name)
                            .code(code)
                            .description(desc)
                            .active(true)
                            .build();
                    return roleRepository.save(r);
                });

        for (FieldSeed fs : fields) {
            if (!roleExtraFieldRepository.findByRoleIdAndFieldNameAndTenantId(role.getId(), fs.name, tenantId).isPresent()) {
                RoleExtraField ref = RoleExtraField.builder()
                        .tenantId(tenantId)
                        .role(role)
                        .fieldName(fs.name)
                        .fieldLabel(fs.label)
                        .fieldType(fs.type)
                        .required(fs.required)
                        .optionsJson(fs.optionsJson)
                        .displayOrder(fs.order)
                        .active(true)
                        .build();
                roleExtraFieldRepository.save(ref);
            }
        }
    }

    private void seedRoleHierarchy(Long tenantId, String childCode, String parentCode) {
        Role child = roleRepository.findByCodeAndTenantId(childCode, tenantId)
                .or(() -> roleRepository.findByNameAndTenantId(childCode, tenantId))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .tenantId(tenantId)
                        .name(childCode)
                        .code(childCode)
                        .description(childCode + " role")
                        .active(true)
                        .build()));

        Role parent = roleRepository.findByCodeAndTenantId(parentCode, tenantId)
                .or(() -> roleRepository.findByNameAndTenantId(parentCode, tenantId))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .tenantId(tenantId)
                        .name(parentCode)
                        .code(parentCode)
                        .description(parentCode + " role")
                        .active(true)
                        .build()));

        if (!roleHierarchyRepository.findByRoleIdAndReportsToRoleIdAndTenantId(child.getId(), parent.getId(), tenantId).isPresent()) {
            RoleHierarchy rh = RoleHierarchy.builder()
                    .tenantId(tenantId)
                    .role(child)
                    .reportsToRole(parent)
                    .build();
            roleHierarchyRepository.save(rh);
        }
    }
}
