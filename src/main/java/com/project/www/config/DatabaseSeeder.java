package com.project.www.config;

import java.util.Set;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.project.www.accessmanagement.entity.Permission;
import com.project.www.accessmanagement.entity.Role;
import com.project.www.accessmanagement.repository.PermissionRepository;
import com.project.www.accessmanagement.repository.RoleExtraFieldRepository;
import com.project.www.accessmanagement.repository.RoleHierarchyRepository;
import com.project.www.accessmanagement.repository.RoleRepository;
import com.project.www.marketing.repository.PipelineStageRepository;
import com.project.www.tenant.entity.Tenant;
import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.entity.TenantSequence;
import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.tenant.repository.TenantSequenceRepository;
import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.repository.UserReportingRepository;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.util.TenantContext;

import com.project.www.tenant.entity.SubscriptionPlan;
import com.project.www.tenant.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final DataSource dataSource;
    private final PipelineStageRepository pipelineStageRepository;
    private final com.project.www.tenant.service.TemplateDefinitionService templateDefinitionService;
    private final com.project.www.accessmanagement.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;
    private final com.project.www.tenant.service.TenantDatabaseService tenantDatabaseService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database seeding check...");

        try (java.sql.Connection conn = dataSource.getConnection()) {
            ResourceDatabasePopulator masterPopulator = new ResourceDatabasePopulator();
            masterPopulator.setContinueOnError(true);
            masterPopulator.addScript(new ClassPathResource("master-schema.sql"));
            masterPopulator.execute(dataSource);
            log.info("Successfully executed master-schema.sql on database");

            // Patch existing tables in master database to add missing columns
            try (java.sql.Statement stmt = conn.createStatement()) {
                // Patch tenants table
                try {
                    stmt.executeUpdate("ALTER TABLE tenants ADD COLUMN super_admin_name VARCHAR(255)");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenants ADD COLUMN phone VARCHAR(255)");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenants ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE'");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenants ADD COLUMN subscription_type VARCHAR(50)");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenants ADD COLUMN subscription_start_date DATE");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenants ADD COLUMN subscription_end_date DATE");
                } catch (Exception e) {
                }

                // Patch tenant_modules
                try {
                    stmt.executeUpdate("ALTER TABLE tenant_modules ADD COLUMN amount DOUBLE");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenant_modules ADD COLUMN payment_method VARCHAR(255)");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenant_modules ADD COLUMN special_requirements TEXT");
                } catch (Exception e) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE tenant_modules ADD COLUMN extra_charges DOUBLE");
                } catch (Exception e) {
                }

                try {
                    stmt.executeUpdate("ALTER TABLE id_format_settings MODIFY COLUMN created_by VARCHAR(255)");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE id_format_settings MODIFY COLUMN updated_by VARCHAR(255)");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate(
                            "ALTER TABLE id_format_settings ADD COLUMN include_year BOOLEAN NOT NULL DEFAULT FALSE");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate(
                            "ALTER TABLE id_format_settings ADD COLUMN prefix VARCHAR(50) NOT NULL DEFAULT 'EMP'");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate(
                            "ALTER TABLE id_format_settings ADD COLUMN padding_length INT NOT NULL DEFAULT 7");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE id_format_settings DROP COLUMN format_string");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate(
                            "ALTER TABLE template_definitions ADD COLUMN is_system_template BOOLEAN NOT NULL DEFAULT FALSE");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate(
                            "ALTER TABLE template_definitions ADD COLUMN is_editable BOOLEAN NOT NULL DEFAULT TRUE");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN stamp_url VARCHAR(500)");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN signature_url VARCHAR(500)");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN header_image_url VARCHAR(500)");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE company_profiles ADD COLUMN footer_image_url VARCHAR(500)");
                } catch (Exception ex) {
                }
                try {
                    stmt.executeUpdate("ALTER TABLE employee_certificates ADD COLUMN custom_html TEXT");
                } catch (Exception ex) {
                }
            } catch (Exception e) {
                log.warn("Failed to patch tables: " + e.getMessage());
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
                            .dbName("rbac_db")
                            .active(true)
                            .build();
                    return tenantRepository.save(t);
                });

        final Long systemTenantId = systemTenant.getId();

        // Load all other tenants into DynamicDataSourceManager so they are available
        // upon restart
        try {
            TenantContext.clear();
            java.util.List<Tenant> allTenants = tenantRepository.findAll();
            for (Tenant t : allTenants) {
                if (!"SYS".equalsIgnoreCase(t.getCode()) && t.getDbName() != null) {
                    log.info("Re-registering existing tenant DB for: " + t.getCode());
                    tenantDatabaseService.registerExistingTenantDatabase(t.getCode(), t.getDbName());

                    // PATCH TENANT DATABASE
                    try (java.sql.Connection tConn = java.sql.DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/" + t.getDbName() + "?createDatabaseIfNotExist=true", "root",
                            "root");
                            java.sql.Statement tStmt = tConn.createStatement()) {
                        try {
                            tStmt.executeUpdate("ALTER TABLE vendor_invoices ADD COLUMN requirement_id BIGINT");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate("ALTER TABLE vendor_invoices ADD COLUMN amount_paid DECIMAL(15,2)");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate("ALTER TABLE vendor_invoices ADD COLUMN amount_pending DECIMAL(15,2)");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate("ALTER TABLE vendor_invoices ADD COLUMN payment_history TEXT");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "ALTER TABLE vendor_invoices ADD COLUMN vendor_contracts ADD COLUMN document_url VARCHAR(500)");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "ALTER TABLE vendor_requirements ADD COLUMN requirement_type VARCHAR(255)");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate("ALTER TABLE vendor_requirements ADD COLUMN return_date DATE");
                        } catch (Exception ex) {
                        }

                        // User set-based relationship tables patch
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS user_permissions (user_id BIGINT NOT NULL, permission_id BIGINT NOT NULL, PRIMARY KEY (user_id, permission_id), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS user_modules (user_id BIGINT NOT NULL, module_name VARCHAR(255) NOT NULL, PRIMARY KEY (user_id, module_name), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }

                        // Marketing Tables
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS campaigns (campaign_id BIGINT AUTO_INCREMENT PRIMARY KEY, campaign_name VARCHAR(150) NOT NULL, subject VARCHAR(200), campaign_type VARCHAR(50), start_date DATE, end_date DATE, budget DECIMAL(19,2) NOT NULL, status VARCHAR(20), description VARCHAR(500), channel VARCHAR(255) NOT NULL, target_audience VARCHAR(255) NOT NULL, audience_filters TEXT, module_type VARCHAR(50), audience_source VARCHAR(50), content TEXT, scheduled_at DATETIME, sent_count INT DEFAULT 0, failed_count INT DEFAULT 0, open_count INT DEFAULT 0, click_count INT DEFAULT 0, archived_at DATETIME) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS campaign_recipients (campaign_id BIGINT NOT NULL, email VARCHAR(255), FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS landing_pages (id BIGINT AUTO_INCREMENT PRIMARY KEY, slug VARCHAR(255) NOT NULL UNIQUE, title VARCHAR(255) NOT NULL, headline VARCHAR(255), subtitle VARCHAR(255), description TEXT, module_type VARCHAR(255), landing_page_type VARCHAR(255), price DECIMAL(19,2), ad_budget DECIMAL(19,2), video_url VARCHAR(255), cta_text VARCHAR(255), created_at DATETIME) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS landing_page_features (landing_page_id BIGINT NOT NULL, feature VARCHAR(255), FOREIGN KEY (landing_page_id) REFERENCES landing_pages(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS campaign_performance (id BIGINT AUTO_INCREMENT PRIMARY KEY, campaign_id BIGINT NOT NULL, impressions INT DEFAULT 0, clicks INT DEFAULT 0, conversions INT DEFAULT 0, spend DECIMAL(19,2), recorded_at DATETIME) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS traffic_events (id BIGINT AUTO_INCREMENT PRIMARY KEY, event_type VARCHAR(255) NOT NULL, source VARCHAR(255), medium VARCHAR(255), campaign_name VARCHAR(255), url VARCHAR(255), ip_address VARCHAR(255), timestamp DATETIME) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS tracked_links (id BIGINT AUTO_INCREMENT PRIMARY KEY, tracked_link_id VARCHAR(255), landing_slug VARCHAR(255), source VARCHAR(255), medium VARCHAR(255), campaign VARCHAR(255), generated_link VARCHAR(255), ad_budget DECIMAL(19,2), timestamp DATETIME) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS email_campaigns (id BIGINT AUTO_INCREMENT PRIMARY KEY, subject VARCHAR(200), body TEXT, status VARCHAR(50), sent_at DATETIME, total_sent INT DEFAULT 0, opened INT DEFAULT 0, clicked INT DEFAULT 0, bounced INT DEFAULT 0, core_campaign_id BIGINT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS coupons (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(50) NOT NULL UNIQUE, discount_type VARCHAR(20) NOT NULL, discount_value DOUBLE NOT NULL, discount_cap DOUBLE, expiry_date DATETIME, max_usage INT, used_count INT DEFAULT 0, min_purchase_amount DOUBLE DEFAULT 0.0, per_user_limit INT DEFAULT 1, is_first_order_only BOOLEAN DEFAULT FALSE, auto_apply BOOLEAN DEFAULT FALSE, affiliate_id BIGINT, learner_id BIGINT, status VARCHAR(20), deleted BOOLEAN DEFAULT FALSE, campaign_id BIGINT, created_by VARCHAR(255), created_at DATETIME) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }
                        try {
                            tStmt.executeUpdate(
                                    "CREATE TABLE IF NOT EXISTS coupon_courses (id BIGINT AUTO_INCREMENT PRIMARY KEY, coupon_id BIGINT NOT NULL, course_id BIGINT NOT NULL, FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                        } catch (Exception ex) {
                        }

                        log.info("Patched tenant database: " + t.getDbName());
                    } catch (Exception e) {
                        log.warn("Failed to patch tenant database: " + t.getDbName() + ", error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load existing tenants into DynamicDataSourceManager: " + e.getMessage());
        }

        // --- SYSTEM PERMISSIONS SEEDING ---
        final String systemTenantCode = systemTenant.getCode();

        // Seed System Modules
        String[] allModules = {
                "ADMIN", "AFFILIATE", "ATTENDANCE", "COURSE", "CRM", "EMPLOYEE", 
                "HRMS", "LEAD", "LMS", "MARKETING", "PAYROLL", "VENDOR", "PO", "PERFORMANCE"
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

        // --- SUBSCRIPTION PLANS SEEDING ---
        if (subscriptionPlanRepository.count() == 0) {
            log.info("Seeding default Subscription Plans...");
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name("Starter")
                    .description("Basic plan for small teams")
                    .monthlyPrice(29.0)
                    .yearlyPrice(290.0)
                    .maxUsers(10)
                    .modules(Set.of("CRM", "EMPLOYEE"))
                    .active(true)
                    .build());
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name("Professional")
                    .description("Advanced features for growing businesses")
                    .monthlyPrice(99.0)
                    .yearlyPrice(990.0)
                    .maxUsers(50)
                    .modules(Set.of("CRM", "HRMS", "VENDOR", "EMPLOYEE"))
                    .active(true)
                    .build());
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name("Enterprise")
                    .description("All-inclusive plan for large organizations")
                    .monthlyPrice(299.0)
                    .yearlyPrice(2990.0)
                    .maxUsers(null) // Unlimited
                    .modules(Set.of("CRM", "HRMS", "VENDOR", "EMPLOYEE", "ADMIN", "MARKETING"))
                    .active(true)
                    .build());
        }

        // Apply schema to ALL existing tenants to ensure missing tables are created
        log.info("Checking permissions and system templates for existing tenants...");
        java.util.List<Tenant> allTenants = tenantRepository.findAll();
        for (Tenant t : allTenants) {
            try {
                TenantContext.setCurrentTenant(t.getId());
                TenantContext.setCurrentTenantCode(t.getCode());

                java.util.List<String[]> newSettingsPerms = java.util.Arrays.asList(
                        new String[] { "COMPANY_PROFILE", "VIEW", "View Company Profile" },
                        new String[] { "COMPANY_PROFILE", "UPDATE", "Update Company Profile" },
                        new String[] { "SETTINGS_MANAGE", "TEMPLATES", "Manage Templates" },
                        new String[] { "SETTINGS_MANAGE", "ONBOARDING", "Manage Onboarding" },
                        new String[] { "SUBSCRIPTION", "MANAGE", "Manage Billing and Subscriptions" },
                        new String[] { "ROLE", "VIEW", "Ability to view roles" },
                        new String[] { "PERMISSION", "VIEW", "Ability to view permissions" },
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
                        new String[] { "PERFORMANCE", "VIEW", "View Vendor Performance" },
                        new String[] { "MARKETING", "VIEW", "View Marketing Campaigns" },
                        new String[] { "MARKETING", "CREATE", "Create Marketing Campaigns" },
                        new String[] { "MARKETING", "UPDATE", "Update Marketing Campaigns" },
                        new String[] { "MARKETING", "DELETE", "Delete Marketing Campaigns" },
                        new String[] { "MARKETING", "ANALYTICS_VIEW", "View Marketing Analytics" },
                        new String[] { "MARKETING", "CAMPAIGN_VIEW", "View Marketing Campaign Analytics" },
                        new String[] { "MARKETING", "ANALYTICS_SUMMARY", "View Marketing Summary" });

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

                Set<String> keysToAssign = newSettingsPerms.stream()
                        .map(info -> (info[0] + "_" + info[1]).toUpperCase())
                        .collect(java.util.stream.Collectors.toSet());

                for (String roleName : new String[] { "SUPER_ADMIN", "TENANT_ADMIN" }) {
                    roleRepository.findByNameAndTenantId(roleName, t.getId()).ifPresent(role -> {
                        Set<Permission> updatedPerms = new java.util.HashSet<>(role.getPermissions());
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

                java.util.List<String> sysCodes = templateDefinitionService.getAvailableSystemTemplates().stream()
                        .map(com.project.www.tenant.entity.TemplateDefinition::getTemplateCode)
                        .collect(java.util.stream.Collectors.toList());
                templateDefinitionService.importSystemTemplates(sysCodes);

            } finally {
                TenantContext.clear();
            }
        }

        try {
            TenantContext.setCurrentTenant(systemTenantId);
            TenantContext.setCurrentTenantCode(systemTenantCode);

            java.util.List<String[]> requiredPermsInfo = java.util.Arrays.asList(
                    new String[] { "TENANT", "CREATE", "Ability to onboard new tenants" },
                    new String[] { "TENANT", "VIEW", "Ability to view tenants" },
                    new String[] { "TENANT", "ENABLE", "Ability to enable tenants" },
                    new String[] { "TENANT", "DISABLE", "Ability to disable tenants" },
                    new String[] { "USER", "CREATE", "Ability to create new users" },
                    new String[] { "USER", "VIEW", "Ability to view users" },
                    new String[] { "USER", "UPDATE", "Ability to update users" },
                    new String[] { "USER", "DELETE", "Ability to delete users" },
                    new String[] { "ROLE", "VIEW", "Ability to view roles" },
                    new String[] { "ROLE", "CREATE", "Ability to create new roles" },
                    new String[] { "ROLE", "UPDATE", "Ability to update roles" },
                    new String[] { "ROLE", "DISABLE", "Ability to disable roles" },
                    new String[] { "ROLE", "ENABLE", "Ability to enable roles" },
                    new String[] { "PERMISSION", "VIEW", "Ability to view permissions" },
                    new String[] { "PERMISSION", "CREATE", "Ability to create permissions" },
                    new String[] { "PERMISSION", "ENABLE", "Ability to enable permissions" },
                    new String[] { "PERMISSION", "DISABLE", "Ability to disable permissions" },
                    new String[] { "SETTINGS", "MANAGE_TEMPLATES", "Ability to manage document templates" },
                    new String[] { "SETTINGS", "MANAGE_ID_FORMATS", "Ability to manage ID generation formats" },
                    new String[] { "COMPANY_PROFILE", "VIEW", "Ability to view company profile" },
                    new String[] { "COMPANY_PROFILE", "UPDATE", "Ability to update company profile" },
                    new String[] { "SUBSCRIPTION", "MANAGE", "Manage Billing and Subscriptions" },
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
                    new String[] { "PERFORMANCE", "VIEW", "View Vendor Performance" },
                    new String[] { "MARKETING", "VIEW", "View Marketing Campaigns" },
                    new String[] { "MARKETING", "CREATE", "Create Marketing Campaigns" },
                    new String[] { "MARKETING", "UPDATE", "Update Marketing Campaigns" },
                    new String[] { "MARKETING", "DELETE", "Delete Marketing Campaigns" },
                    new String[] { "MARKETING", "ANALYTICS_VIEW", "View Marketing Analytics" },
                    new String[] { "MARKETING", "CAMPAIGN_VIEW", "View Marketing Campaign Analytics" },
                    new String[] { "MARKETING", "ANALYTICS_SUMMARY", "View Marketing Summary" });

            java.util.List<Permission> existingPerms = permissionRepository.findAllByTenantId(systemTenantId);
            java.util.Map<String, Permission> permMap = existingPerms.stream()
                    .collect(java.util.stream.Collectors.toMap(Permission::getPermissionKey,
                            java.util.function.Function.identity()));

            java.util.Set<Permission> superAdminPermissions = new java.util.HashSet<>();
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
                log.info("System seeding completed. Super Admin user 'superadmin@system.com' created");
            } else {
                User superAdmin = userRepository.findByEmailAndTenantId("superadmin@system.com", systemTenantId).get();
                superAdmin.setRole(superAdminRole);
                userRepository.save(superAdmin);
                log.info("System seeding updated. Super Admin role updated");
            }

        } finally {
            TenantContext.clear();
        }

        java.util.List<Tenant> everyTenant = tenantRepository.findAll();
        globalUserRegistrySyncService.syncAllTenants(everyTenant, userRepository);
    }
}
