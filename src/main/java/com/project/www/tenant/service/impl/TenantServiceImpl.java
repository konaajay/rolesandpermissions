package com.project.www.tenant.service.impl;

import com.project.www.marketing.repository.PipelineStageRepository;

import com.project.www.service.EmailService;

import com.project.www.accessmanagement.entity.Role;

import com.project.www.accessmanagement.entity.Permission;

import com.project.www.accessmanagement.repository.RoleRepository;

import com.project.www.accessmanagement.repository.PermissionRepository;

import com.project.www.tenant.entity.TenantSettings;

import com.project.www.tenant.entity.TenantModule;

import com.project.www.tenant.entity.Tenant;

import com.project.www.tenant.entity.TemplateDefinition;

import com.project.www.tenant.repository.TenantSettingsRepository;

import com.project.www.tenant.repository.TenantRepository;

import com.project.www.tenant.repository.TenantModuleRepository;

import com.project.www.tenant.service.TenantDatabaseService;

import com.project.www.tenant.service.TemplateDefinitionService;

import com.project.www.accessmanagement.entity.User;

import com.project.www.accessmanagement.repository.UserRepository;

import com.project.www.accessmanagement.repository.GlobalUserRegistryRepository;

import com.project.www.accessmanagement.service.GlobalUserRegistrySyncService;

import com.project.www.vendor.entity.Vendor;

import com.project.www.tenant.dto.CreateTenantRequest;
import com.project.www.tenant.dto.TenantResponse;
import com.project.www.entity.*;
import com.project.www.repository.*;
import com.project.www.tenant.service.TenantService;

import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final com.project.www.accessmanagement.repository.GlobalUserRegistryRepository globalUserRegistryRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;
    private final PipelineStageRepository pipelineStageRepository;
    private final com.project.www.tenant.service.TemplateDefinitionService templateDefinitionService;
    private final com.project.www.accessmanagement.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;
    private final com.project.www.tenant.service.TenantDatabaseService tenantDatabaseService;
    private final com.project.www.service.EmailService emailService;
    
    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (request.getTenantName() == null || request.getTenantName().trim().isEmpty()) {
            throw new RuntimeException("Tenant name must be provided");
        }

        String tenantCode = request.getTenantCode();
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            String cleanName = request.getTenantName().replaceAll("[^a-zA-Z]", "");
            if (cleanName.length() >= 3) {
                tenantCode = cleanName.substring(0, 3).toUpperCase();
            } else {
                tenantCode = cleanName.toUpperCase() + "T";
            }
        } else {
            tenantCode = tenantCode.trim().toUpperCase();
        }

        final String finalTenantCode = tenantCode;
        final String dbName = "tenant_" + tenantCode.toLowerCase();

        String originalTenantCode = TenantContext.getCurrentTenantCode();
        Long originalTenantId = TenantContext.getCurrentTenant();

        // MUST clear TenantContext before ANY master-DB operation so the
        // DynamicDataSourceManager routes to the master datasource.
        TenantContext.clear();

        try {
            // Guard: email must be globally unique across all tenants (master DB query)
            if (globalUserRegistrySyncService.existsByEmail(request.getAdminEmail())) {
                throw new RuntimeException("Email already exists in another workspace. Please use a unique email.");
            }

            // Check if tenant already exists in master db
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            boolean exists = transactionTemplate
                    .execute(status -> tenantRepository.existsByName(request.getTenantName())
                            || tenantRepository.existsByCode(finalTenantCode));
            if (exists) {
                throw new RuntimeException("Tenant name or code already exists");
            }

            // 4. Save Tenant Entity (Master)
            Tenant tenant = transactionTemplate.execute(status -> {
                Tenant t = Tenant.builder()
                        .name(request.getTenantName())
                        .code(finalTenantCode)
                        .dbName(dbName)
                        .domain(request.getDomain())
                        .adminEmail(request.getAdminEmail())
                        .superAdminName(request.getAdminFirstName() + " " + request.getAdminLastName())
                        .phone(request.getPhone())
                        .status("TRIAL")
                        .subscriptionType("TRIAL")
                        .subscriptionStartDate(java.time.LocalDate.now())
                        .subscriptionEndDate(java.time.LocalDate.now().plusDays(30))
                        .active(true)
                        .build();
                return tenantRepository.save(t);
            });

            // 5. Switch context and seed tenant-specific data
            try {
                TenantContext.setCurrentTenant(tenant.getId());
                TenantContext.setCurrentTenantCode(finalTenantCode);

                // Provision Isolated Database dynamically
                tenantDatabaseService.provisionTenantDatabase(finalTenantCode, dbName);
                transactionTemplate.executeWithoutResult(status -> {
                    List<String[]> defaultPermsInfo = Arrays.asList(
                            new String[] { "USER", "CREATE", "Ability to create new users" },
                            new String[] { "USER", "VIEW", "Ability to view users" },
                            new String[] { "USER", "UPDATE", "Ability to update users" },
                            new String[] { "USER", "DELETE", "Ability to delete users" },
                            new String[] { "ROLE", "CREATE", "Ability to create new roles" },
                            new String[] { "ROLE", "VIEW", "Ability to view roles" },
                            new String[] { "ROLE", "UPDATE", "Ability to update roles" },
                            new String[] { "ROLE", "DISABLE", "Ability to disable roles" },
                            new String[] { "ROLE", "ENABLE", "Ability to enable roles" },
                            new String[] { "PERMISSION", "CREATE", "Ability to create permissions" },
                            new String[] { "PERMISSION", "VIEW", "Ability to view permissions" },
                            new String[] { "PERMISSION", "ENABLE", "Ability to enable permissions" },
                            new String[] { "PERMISSION", "DISABLE", "Ability to disable permissions" },
                            new String[] { "REPORT", "VIEW", "Ability to view reports" },
                            new String[] { "COMPANY_PROFILE", "VIEW", "View Company Profile" },
                            new String[] { "COMPANY_PROFILE", "UPDATE", "Update Company Profile" },
                            new String[] { "SETTINGS_MANAGE", "TEMPLATES", "Manage Templates" },
                            new String[] { "SETTINGS_MANAGE", "ONBOARDING", "Manage Onboarding" },
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
                            new String[] { "MARKETING", "DELETE", "Delete Marketing Campaigns" });

                    List<Permission> existingPerms = permissionRepository.findAllByTenantId(tenant.getId());
                    java.util.Map<String, Permission> permMap = existingPerms.stream()
                            .collect(java.util.stream.Collectors.toMap(Permission::getPermissionKey,
                                    java.util.function.Function.identity()));

                    List<Permission> permsToSave = new java.util.ArrayList<>();
                    for (String[] permInfo : defaultPermsInfo) {
                        String key = (permInfo[0] + "_" + permInfo[1]).toUpperCase();
                        if (!permMap.containsKey(key)) {
                            permsToSave.add(Permission.builder()
                                    .tenantId(tenant.getId())
                                    .module(permInfo[0])
                                    .action(permInfo[1])
                                    .description(permInfo[2])
                                    .active(true)
                                    .build());
                        }
                    }

                    List<Permission> savedPerms = new java.util.ArrayList<>(existingPerms);
                    if (!permsToSave.isEmpty()) {
                        savedPerms.addAll(permissionRepository.saveAll(permsToSave));
                    }
                    Set<Permission> adminPermissions = new java.util.HashSet<>(savedPerms);

                    // Create default roles: SUPER_ADMIN
                    Role superAdminRole = roleRepository.findByNameAndTenantId("SUPER_ADMIN", tenant.getId())
                            .orElseGet(() -> Role.builder()
                                    .tenantId(tenant.getId())
                                    .name("SUPER_ADMIN")
                                    .code("SUPER_ADMIN")
                                    .description("Administrator role with all permissions")
                                    .active(true)
                                    .build());
                    superAdminRole.setPermissions(adminPermissions);
                    superAdminRole = roleRepository.save(superAdminRole);



                    // Initialize Settings and Sequence for Employee IDs
                    int currentYear = java.time.LocalDate.now().getYear();
                    TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                            .orElseGet(() -> TenantSettings.builder()
                                    .tenantId(tenant.getId())
                                    .employeeSequence(0L)
                                    .leadSequence(0L)
                                    .build());
                    settings.setEmployeeSequence(settings.getEmployeeSequence() + 1);
                    tenantSettingsRepository.save(settings);

                    User adminUser = userRepository.findFirstByEmailAndTenantId(request.getAdminEmail(), tenant.getId())
                            .orElseGet(() -> User.builder()
                                    .tenantId(tenant.getId())
                                    .firstName(request.getAdminFirstName())
                                    .lastName(request.getAdminLastName())
                                    .email(request.getAdminEmail())
                                    .password(passwordEncoder.encode(request.getAdminPassword()))
                                    .active(true)
                                    .build());
                    adminUser.setRole(superAdminRole);
                    userRepository.save(adminUser);
                    try {
                        TenantContext.clear();
                        globalUserRegistrySyncService.syncUser(adminUser, tenant.getId());
                    } finally {
                        TenantContext.setCurrentTenant(tenant.getId());
                        TenantContext.setCurrentTenantCode(finalTenantCode);
                    }

                    // Seed default system templates
                    List<String> sysCodes = templateDefinitionService.getAvailableSystemTemplates().stream()
                            .map(com.project.www.tenant.entity.TemplateDefinition::getTemplateCode)
                            .collect(Collectors.toList());
                    templateDefinitionService.importSystemTemplates(sysCodes);
                });

            } finally {
                TenantContext.clear();
            }

            // 6. Give the new tenant ALL available modules by default
            java.lang.reflect.Field[] moduleFields = com.project.www.constants.Modules.class.getDeclaredFields();
            for (java.lang.reflect.Field field : moduleFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                    try {
                        String moduleName = (String) field.get(null);
                        tenantModuleRepository.save(TenantModule.builder()
                                .tenantId(tenant.getId())
                                .moduleName(moduleName)
                                .active(true)
                                .build());
                    } catch (IllegalAccessException e) {
                        // ignore
                    }
                }
            }

            try {
                String loginUrl = frontendUrl + "/login";
                emailService.sendTenantWelcomeEmail(
                    request.getAdminEmail(),
                    request.getAdminFirstName(),
                    request.getTenantName(),
                    tenant.getDomain(),
                    request.getAdminPassword(),
                    loginUrl
                );
            } catch (Exception e) {
                log.error("Failed to send welcome email to tenant admin: {}", request.getAdminEmail(), e);
            }

            return TenantResponse.builder()
                    .id(tenant.getId())
                    .name(tenant.getName())
                    .code(tenant.getCode())
                    .domain(tenant.getDomain())
                    .active(tenant.getActive())
                    .adminEmail(request.getAdminEmail())
                    .build();
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
            TenantContext.setCurrentTenantCode(originalTenantCode);
        }
    }

    private TenantResponse mapToResponse(Tenant t) {
        return TenantResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .code(t.getCode())
                .domain(t.getDomain())
                .dbName(t.getDbName())
                .adminEmail(t.getAdminEmail())
                .superAdminName(t.getSuperAdminName())
                .phone(t.getPhone())
                .status(t.getStatus())
                .subscriptionType(t.getSubscriptionType())
                .subscriptionStartDate(t.getSubscriptionStartDate())
                .subscriptionEndDate(t.getSubscriptionEndDate())
                .active(t.getActive())
                .build();
    }

    @Override
    public List<TenantResponse> getAllTenants() {
        String originalTenantCode = TenantContext.getCurrentTenantCode();
        Long originalTenantId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            return tenantRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
            TenantContext.setCurrentTenantCode(originalTenantCode);
        }
    }

    @Override
    public void enableTenant(Long id) {
        String originalTenantCode = TenantContext.getCurrentTenantCode();
        Long originalTenantId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            Tenant tenant = tenantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            tenant.setActive(true);
            tenantRepository.save(tenant);
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
            TenantContext.setCurrentTenantCode(originalTenantCode);
        }
    }

    @Override
    public void disableTenant(Long id) {
        String originalTenantCode = TenantContext.getCurrentTenantCode();
        Long originalTenantId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            Tenant tenant = tenantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));

            // Prevent disabling the master system tenant (ID 1)
            if (tenant.getId() == 1L || "SYS".equalsIgnoreCase(tenant.getCode())
                    || "SYSTEM".equalsIgnoreCase(tenant.getCode())) {
                throw new IllegalArgumentException("Cannot disable the master system tenant.");
            }

            tenant.setActive(false);
            tenantRepository.save(tenant);
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
            TenantContext.setCurrentTenantCode(originalTenantCode);
        }
    }

}
