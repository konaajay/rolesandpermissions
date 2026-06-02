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
    private final com.project.www.service.TemplateDefinitionService templateDefinitionService;
    private final com.project.www.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database seeding check...");

        try (java.sql.Connection conn = masterDataSource.getConnection()) {
            ResourceDatabasePopulator masterPopulator = new ResourceDatabasePopulator();
            masterPopulator.addScript(new ClassPathResource("master-schema.sql"));
            masterPopulator.execute(masterDataSource);
            log.info("Successfully executed master-schema.sql on rbac_db");

            // Patch existing tables in master database to add missing columns
            try (java.sql.Statement stmt = conn.createStatement()) {
                // Patch tenants table
                try { stmt.executeUpdate("ALTER TABLE rbac_db.tenants ADD COLUMN super_admin_name VARCHAR(255)"); } catch (Exception e) {}
                try { stmt.executeUpdate("ALTER TABLE rbac_db.tenants ADD COLUMN phone VARCHAR(255)"); } catch (Exception e) {}
                try { stmt.executeUpdate("ALTER TABLE rbac_db.tenants ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE'"); } catch (Exception e) {}
                try { stmt.executeUpdate("ALTER TABLE rbac_db.tenants ADD COLUMN subscription_type VARCHAR(50)"); } catch (Exception e) {}
                try { stmt.executeUpdate("ALTER TABLE rbac_db.tenants ADD COLUMN subscription_start_date DATE"); } catch (Exception e) {}
                try { stmt.executeUpdate("ALTER TABLE rbac_db.tenants ADD COLUMN subscription_end_date DATE"); } catch (Exception e) {}

                // Patch tenant_modules
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN amount DOUBLE");
                    log.info("Patched amount for tenant_modules");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN payment_method VARCHAR(255)");
                    log.info("Patched payment_method for tenant_modules");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN special_requirements TEXT");
                    log.info("Patched special_requirements for tenant_modules");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE rbac_db.tenant_modules ADD COLUMN extra_charges DOUBLE");
                    log.info("Patched extra_charges for tenant_modules");
                } catch (Exception e) {
                }
            } catch (Exception e) {
                log.warn("Failed to patch tenant_modules: " + e.getMessage());
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
                com.project.www.constants.Modules.CRM,
                com.project.www.constants.Modules.HRMS,
                com.project.www.constants.Modules.ADMIN,
                com.project.www.constants.Modules.VENDOR
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

        // Apply schema to ALL existing tenants to ensure missing tables (like
        // id_format_settings) are created
        log.info("Applying schema.sql to all existing tenant databases...");
        java.util.List<Tenant> allTenants = tenantRepository.findAll();
        for (Tenant t : allTenants) {
            if (t.getDbName() == null || t.getDbName().isEmpty())
                continue;
            try {
                DataSource ds;
                if (rds.containsDataSource(t.getCode())) {
                    ds = rds.getDataSource(t.getCode());
                } else {
                    ds = dataSourceConfig.createTenantDataSource(t.getDbName(), null, null);
                    rds.addDataSource(t.getCode(), ds);
                }
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("schema.sql"));
                populator.execute(ds);

                // Also explicitly patch any tables that might have been created with incorrect
                // column types earlier
                try (java.sql.Connection tConn = ds.getConnection();
                        java.sql.Statement tStmt = tConn.createStatement()) {
                    try {
                        tStmt.executeUpdate("ALTER TABLE id_format_settings MODIFY COLUMN created_by VARCHAR(255)");
                        tStmt.executeUpdate("ALTER TABLE id_format_settings MODIFY COLUMN updated_by VARCHAR(255)");
                    } catch (Exception ex) {
                        // ignore if table doesn't exist
                    }
                    try {
                        tStmt.executeUpdate(
                                "ALTER TABLE id_format_settings ADD COLUMN include_year BOOLEAN NOT NULL DEFAULT FALSE");
                    } catch (Exception ex) {
                        // ignore if column already exists
                    }
                    try {
                        tStmt.executeUpdate(
                                "ALTER TABLE id_format_settings ADD COLUMN prefix VARCHAR(50) NOT NULL DEFAULT 'EMP'");
                        tStmt.executeUpdate(
                                "ALTER TABLE id_format_settings ADD COLUMN padding_length INT NOT NULL DEFAULT 7");
                        tStmt.executeUpdate("ALTER TABLE id_format_settings DROP COLUMN format_string");
                    } catch (Exception ex) {
                        // ignore if already updated
                    }
                    try {
                        tStmt.executeUpdate(
                                "ALTER TABLE template_definitions ADD COLUMN is_system_template BOOLEAN NOT NULL DEFAULT FALSE");
                        tStmt.executeUpdate(
                                "ALTER TABLE template_definitions ADD COLUMN is_editable BOOLEAN NOT NULL DEFAULT TRUE");
                    } catch (Exception ex) {
                        // ignore if already updated
                    }
                    try {
                        tStmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN stamp_url VARCHAR(500)");
                    } catch (Exception ex) {}
                    try {
                        tStmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN signature_url VARCHAR(500)");
                    } catch (Exception ex) {}
                    try {
                        tStmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN header_image_url VARCHAR(500)");
                    } catch (Exception ex) {}
                    try {
                        tStmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN footer_image_url VARCHAR(500)");
                    } catch (Exception ex) {}
                    try {
                        tStmt.executeUpdate("ALTER TABLE employee_certificates ADD COLUMN custom_html TEXT");
                    } catch (Exception ex) {}
                }

                log.info("Successfully applied schema.sql and patches to tenant: {}", t.getCode());

                // Also ensure the tenant has the new Settings permissions
                try {
                    TenantContext.setCurrentTenant(t.getId());
                    TenantContext.setCurrentTenantCode(t.getCode());

                    java.util.List<String[]> newSettingsPerms = java.util.Arrays.asList(
                            new String[] { "COMPANY_PROFILE", "VIEW", "View Company Profile" },
                            new String[] { "COMPANY_PROFILE", "UPDATE", "Update Company Profile" },
                            new String[] { "SETTINGS_MANAGE", "TEMPLATES", "Manage Templates" },
                            new String[] { "SETTINGS_MANAGE", "ONBOARDING", "Manage Onboarding" });

                    java.util.List<Permission> existingTenantPerms = permissionRepository.findAllByTenantId(t.getId());
                    java.util.Map<String, Permission> permMap = existingTenantPerms.stream()
                            .collect(java.util.stream.Collectors.toMap(Permission::getPermissionKey,
                                    java.util.function.Function.identity()));

                    java.util.List<Permission> permsToSave = new java.util.ArrayList<>();
                    for (String[] permInfo : newSettingsPerms) {
                        String key = (permInfo[0] + "_" + permInfo[1]).toUpperCase();
                        if (!permMap.containsKey(key)) {
                            Permission p = Permission.builder()
                                    .tenantId(t.getId())
                                    .module(permInfo[0])
                                    .action(permInfo[1])
                                    .description(permInfo[2])
                                    .active(true)
                                    .build();
                            permsToSave.add(p);
                        }
                    }
                    if (!permsToSave.isEmpty()) {
                        java.util.List<Permission> saved = permissionRepository.saveAll(permsToSave);
                        for (Permission s : saved) {
                            permMap.put(s.getPermissionKey(), s);
                        }
                    }

                    // Assign these permissions to SUPER_ADMIN and TENANT_ADMIN if they don't have
                    // them
                    Set<String> keysToAssign = newSettingsPerms.stream()
                            .map(info -> (info[0] + "_" + info[1]).toUpperCase())
                            .collect(java.util.stream.Collectors.toSet());

                    for (String roleName : new String[] { "SUPER_ADMIN", "TENANT_ADMIN" }) {
                        roleRepository.findByNameAndTenantId(roleName, t.getId()).ifPresent(role -> {
                            Set<Permission> updatedPerms = new HashSet<>(role.getPermissions());
                            boolean changed = false;
                            for (String key : keysToAssign) {
                                Permission p = permMap.get(key);
                                if (p != null && !updatedPerms.contains(p)) {
                                    updatedPerms.add(p);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                role.setPermissions(updatedPerms);
                                roleRepository.save(role);
                            }
                        });
                    }
                    log.info("Patched missing Settings permissions for tenant: {}", t.getCode());

                    // Seed missing system templates for existing tenants
                    java.util.List<String> sysCodes = templateDefinitionService.getAvailableSystemTemplates().stream()
                            .map(com.project.www.entity.TemplateDefinition::getTemplateCode)
                            .collect(java.util.stream.Collectors.toList());
                    templateDefinitionService.importSystemTemplates(sysCodes);
                    log.info("Patched system templates for tenant: {}", t.getCode());

                } finally {
                    TenantContext.clear();
                }

            } catch (Exception e) {
                log.warn("Failed to apply schema to tenant {}: {}", t.getCode(), e.getMessage());
            }
        }

        // 4. Switch context to System Tenant and seed roles/permissions/user in
        // tenant_sys
        try {
            TenantContext.setCurrentTenant(systemTenantId);
            TenantContext.setCurrentTenantCode(systemTenantCode);

            // Pre-load roles into cache
            java.util.Map<String, Role> cachedRoles = new java.util.HashMap<>();
            roleRepository.findAllByTenantId(systemTenantId).forEach(r -> {
                cachedRoles.put(r.getCode(), r);
                cachedRoles.put(r.getName(), r);
            });

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
                    new String[] { "SETTINGS", "MANAGE_TEMPLATES", "Ability to manage document templates" },
                    new String[] { "SETTINGS", "MANAGE_ID_FORMATS", "Ability to manage ID generation formats" },
                    new String[] { "COMPANY_PROFILE", "VIEW", "Ability to view company profile" },
                    new String[] { "COMPANY_PROFILE", "UPDATE", "Ability to update company profile" },
                    new String[] { "DASHBOARD", "VIEW", "View Dashboard" },
                    new String[] { "VENDOR", "CREATE", "Create Vendors" },
                    new String[] { "VENDOR", "VIEW", "View Vendors" },
                    new String[] { "VENDOR", "UPDATE", "Update Vendors" },
                    new String[] { "VENDOR", "DELETE", "Delete Vendors" },
                    new String[] { "VENDOR", "INVOICE_CREATE", "Create Vendor Invoices" },
                    new String[] { "VENDOR", "INVOICE_VIEW", "View Vendor Invoices" },
                    new String[] { "VENDOR", "INVOICE_UPDATE", "Update Vendor Invoices" },
                    new String[] { "VENDOR", "INVOICE_DELETE", "Delete Vendor Invoices" },
                    new String[] { "VENDOR", "CONTRACT_CREATE", "Create Vendor Contracts" },
                    new String[] { "VENDOR", "CONTRACT_VIEW", "View Vendor Contracts" },
                    new String[] { "VENDOR", "CONTRACT_UPDATE", "Update Vendor Contracts" },
                    new String[] { "VENDOR", "CONTRACT_DELETE", "Delete Vendor Contracts" },
                    new String[] { "VENDOR", "CATEGORY_CREATE", "Create Vendor Categories" },
                    new String[] { "VENDOR", "CATEGORY_VIEW", "View Vendor Categories" },
                    new String[] { "VENDOR", "CATEGORY_UPDATE", "Update Vendor Categories" },
                    new String[] { "VENDOR", "CATEGORY_DELETE", "Delete Vendor Categories" },
                    new String[] { "VENDOR", "AUDIT_CREATE", "Create Vendor Audits" },
                    new String[] { "VENDOR", "AUDIT_VIEW", "View Vendor Audits" },
                    new String[] { "VENDOR", "AUDIT_UPDATE", "Update Vendor Audits" },
                    new String[] { "VENDOR", "AUDIT_DELETE", "Delete Vendor Audits" },
                    new String[] { "PO", "CREATE", "Create Purchase Orders" },
                    new String[] { "PO", "VIEW", "View Purchase Orders" },
                    new String[] { "PO", "UPDATE", "Update Purchase Orders" },
                    new String[] { "PO", "DELETE", "Delete Purchase Orders" },
                    new String[] { "PERFORMANCE", "VIEW", "View Vendor Performance" });

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

        } finally {
            TenantContext.clear();
        }

        // Run the GlobalUserRegistry Synchronization across all tenants
        java.util.List<Tenant> everyTenant = tenantRepository.findAll();
        globalUserRegistrySyncService.syncAllTenants(everyTenant, userRepository);
    }
}
