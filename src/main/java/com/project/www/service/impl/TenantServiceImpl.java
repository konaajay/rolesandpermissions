package com.project.www.service.impl;

import com.project.www.dto.CreateTenantRequest;
import com.project.www.dto.TenantResponse;
import com.project.www.entity.*;
import com.project.www.repository.*;
import com.project.www.service.TenantService;
import com.project.www.config.DataSourceConfig;
import com.project.www.config.TenantRoutingDataSource;
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

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSourceConfig dataSourceConfig;
    private final DataSource routingDataSource;
    private final DataSource masterDataSource;
    private final PlatformTransactionManager transactionManager;
    private final PipelineStageRepository pipelineStageRepository;
    private final com.project.www.service.TemplateDefinitionService templateDefinitionService;

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

        try {
            // Master Database Operations MUST have clear TenantContext
            TenantContext.clear();

            // Check if tenant already exists in master db
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            boolean exists = transactionTemplate
                    .execute(status -> tenantRepository.existsByName(request.getTenantName())
                            || tenantRepository.existsByCode(finalTenantCode));
            if (exists) {
                throw new RuntimeException("Tenant name or code already exists");
            }

            // 1. Create database dynamically on master server
            try (Connection connection = masterDataSource.getConnection();
                    Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create tenant database: " + e.getMessage(), e);
            }

            // 2. Build tenant datasource and execute schema script
            DataSource tenantDs = dataSourceConfig.createTenantDataSource(dbName, null, null);
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("schema.sql"));
                populator.execute(tenantDs);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize database schema: " + e.getMessage(), e);
            }

            // 3. Register dynamic datasource
            TenantRoutingDataSource rds = (TenantRoutingDataSource) routingDataSource;
            rds.addDataSource(finalTenantCode, tenantDs);

            // 4. Save Tenant Entity (Master)
            Tenant tenant = transactionTemplate.execute(status -> {
                Tenant t = Tenant.builder()
                        .name(request.getTenantName())
                        .code(finalTenantCode)
                        .dbName(dbName)
                        .adminEmail(request.getAdminEmail())
                        .active(true)
                        .build();
                return tenantRepository.save(t);
            });

            // 5. Switch context and seed tenant-specific data
            try {
                TenantContext.setCurrentTenant(tenant.getId());
                TenantContext.setCurrentTenantCode(finalTenantCode);

                transactionTemplate.executeWithoutResult(status -> {
                    List<String[]> defaultPermsInfo = Arrays.asList(
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
                            new String[] { "COMPANY_PROFILE", "VIEW", "View Company Profile" },
                            new String[] { "COMPANY_PROFILE", "UPDATE", "Update Company Profile" },
                            new String[] { "SETTINGS_MANAGE", "TEMPLATES", "Manage Templates" },
                            new String[] { "SETTINGS_MANAGE", "ONBOARDING", "Manage Onboarding" }
                    );

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
                    Role adminRole = roleRepository.findByNameAndTenantId("SUPER_ADMIN", tenant.getId())
                            .orElseGet(() -> Role.builder()
                                    .tenantId(tenant.getId())
                                    .name("SUPER_ADMIN")
                                    .code("SUPER_ADMIN")
                                    .description("Administrator role with all permissions")
                                    .active(true)
                                    .build());
                    adminRole.setPermissions(adminPermissions);
                    adminRole = roleRepository.save(adminRole);

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

                    // Create Admin User
                    String employeeId = settings.getEmployeeIdFormat() != null
                            && !settings.getEmployeeIdFormat().trim().isEmpty()
                                    ? settings.getEmployeeIdFormat()
                                            .replace("{TENANT}", finalTenantCode)
                                            .replace("{YYYY}", String.valueOf(currentYear))
                                            .replace("{SEQ}", String.format("%03d", settings.getEmployeeSequence()))
                                    : String.format("EMP-%s-%d-%03d", finalTenantCode, currentYear,
                                            settings.getEmployeeSequence());

                    User adminUser = userRepository.findByEmailAndTenantId(request.getAdminEmail(), tenant.getId())
                            .orElseGet(() -> User.builder()
                                    .tenantId(tenant.getId())
                                    .firstName(request.getAdminFirstName())
                                    .lastName(request.getAdminLastName())
                                    .email(request.getAdminEmail())
                                    .password(passwordEncoder.encode(request.getAdminPassword()))
                                    .employeeId(employeeId)
                                    .active(true)
                                    .build());
                    adminUser.setRole(adminRole);
                    userRepository.save(adminUser);

                    // Seed default system templates
                    List<String> sysCodes = templateDefinitionService.getAvailableSystemTemplates().stream()
                            .map(com.project.www.entity.TemplateDefinition::getTemplateCode)
                            .collect(Collectors.toList());
                    templateDefinitionService.importSystemTemplates(sysCodes);
                });

            } finally {
                TenantContext.clear();
            }

            // 6. Give the new tenant the ADMIN and EMPLOYEE modules by default
            tenantModuleRepository.save(TenantModule.builder()
                    .tenantId(tenant.getId())
                    .moduleName(com.project.www.constants.Modules.ADMIN)
                    .active(true)
                    .build());
            tenantModuleRepository.save(TenantModule.builder()
                    .tenantId(tenant.getId())
                    .moduleName(com.project.www.constants.Modules.EMPLOYEE)
                    .active(true)
                    .build());

            return TenantResponse.builder()
                    .id(tenant.getId())
                    .name(tenant.getName())
                    .code(tenant.getCode())
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
                .dbName(t.getDbName())
                .adminEmail(t.getAdminEmail())
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
