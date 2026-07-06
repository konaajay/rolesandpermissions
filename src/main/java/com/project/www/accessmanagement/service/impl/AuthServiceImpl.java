package com.project.www.accessmanagement.service.impl;

import com.project.www.accessmanagement.dto.AuthResponse;
import com.project.www.tenant.dto.CreateTenantRequest;
import com.project.www.accessmanagement.dto.LoginRequest;
import com.project.www.accessmanagement.dto.RegisterRequest;
import com.project.www.tenant.dto.TenantResponse;
import com.project.www.accessmanagement.entity.Role;
import com.project.www.tenant.entity.Tenant;
import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.exception.UserAlreadyExistsException;
import com.project.www.tenant.entity.TenantSettings;
import com.project.www.tenant.repository.TenantSettingsRepository;
import com.project.www.accessmanagement.repository.RoleRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.security.JwtService;
import com.project.www.accessmanagement.service.AuthService;
import com.project.www.tenant.service.TenantService;
import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.tenant.entity.TenantModule;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.project.www.util.TenantResolver tenantResolver;
    private final jakarta.servlet.http.HttpServletRequest servletRequest;

    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private TenantService tenantService;

    private final TenantModuleRepository tenantModuleRepository;
    private final com.project.www.accessmanagement.repository.PermissionRepository permissionRepository;
    private final com.project.www.accessmanagement.repository.GlobalUserRegistryRepository globalUserRegistryRepository;
    private final com.project.www.accessmanagement.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String originalTenantCode = TenantContext.getCurrentTenantCode();
        Long originalTenantId = TenantContext.getCurrentTenant();
        Tenant tenant = null;

        try {
            TenantContext.clear(); // Ensure we query the master database

            // 1. Locate user in global registry by email and tenant code if provided
            com.project.www.accessmanagement.entity.GlobalUserRegistry registryEntry;
            if (originalTenantCode != null && !originalTenantCode.trim().isEmpty()) {
                registryEntry = globalUserRegistryRepository.findByEmailAndTenantCode(request.getEmail(), originalTenantCode)
                        .orElseThrow(() -> new RuntimeException("No account found with this email address in this workspace."));
            } else {
                registryEntry = globalUserRegistryRepository.findFirstByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("No account found with this email address."));
            }

            if (!registryEntry.getActive()) {
                throw new RuntimeException("User account is inactive.");
            }

            // 2. Fetch the tenant from the master database
            tenant = tenantRepository.findById(registryEntry.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Associated workspace not found."));

            if (tenant != null) {
                Set<String> activeModules = tenantModuleRepository.findByTenantIdAndActiveTrue(tenant.getId())
                        .stream()
                        .map(TenantModule::getModuleName)
                        .collect(Collectors.toSet());
                servletRequest.setAttribute("tenant_modules", activeModules);
            }
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
            TenantContext.setCurrentTenantCode(originalTenantCode);
        }

        // ==========================================
        // PHASE 3: SaaS Paywall & Trial Enforcement
        // ==========================================
        if ("TRIAL".equalsIgnoreCase(tenant.getStatus()) && tenant.getSubscriptionEndDate() != null) {
            if (java.time.LocalDate.now().isAfter(tenant.getSubscriptionEndDate())) {
                tenant.setStatus("EXPIRED");
                tenant.setActive(false);
                // Update in master DB
                String ogCode = TenantContext.getCurrentTenantCode();
                try {
                    TenantContext.clear();
                    tenantRepository.save(tenant);
                } finally {
                    TenantContext.setCurrentTenantCode(ogCode);
                }
                throw new RuntimeException(
                        "PAYMENT_REQUIRED: Your 15-day free trial has expired. Please upgrade your subscription to continue.");
            }
        } else if ("ACTIVE".equalsIgnoreCase(tenant.getStatus()) && tenant.getSubscriptionEndDate() != null) {
            if (java.time.LocalDate.now().isAfter(tenant.getSubscriptionEndDate())) {
                tenant.setStatus("EXPIRED");
                // Do not set active=false immediately, maybe give a grace period or just expire
                String ogCode = TenantContext.getCurrentTenantCode();
                try {
                    TenantContext.clear();
                    tenantRepository.save(tenant);
                } finally {
                    TenantContext.setCurrentTenantCode(ogCode);
                }
                throw new RuntimeException(
                        "PAYMENT_REQUIRED: Your subscription has expired. Please renew your plan to continue.");
            }
        } else if ("EXPIRED".equalsIgnoreCase(tenant.getStatus())) {
            throw new RuntimeException(
                    "PAYMENT_REQUIRED: Your subscription has expired. Please renew your plan to continue.");
        }

        if (!tenant.getActive()) {
            throw new RuntimeException("This workspace has been disabled by the system administrator.");
        }

        // Set tenant context for authentication loading
        TenantContext.setCurrentTenant(tenant.getId());
        TenantContext.setCurrentTenantCode(tenant.getCode());
        try {
            User user = userRepository.findFirstByEmailAndTenantId(request.getEmail(), tenant.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "No account found with this email address in the tenant workspace."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Incorrect password.");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            if (!user.getActive()) {
                throw new RuntimeException("User account is inactive");
            }

            if (user.getRole() == null || !user.getRole().getActive()) {
                throw new RuntimeException("User role is disabled or not assigned");
            }

            @SuppressWarnings("unchecked")
            Set<String> modules = (Set<String>) servletRequest.getAttribute("tenant_modules");
            if (modules == null) {
                modules = new HashSet<>();
            }

            java.util.List<String> coreModules = java.util.Arrays.asList("USER", "ROLE", "TENANT", "PERMISSION",
                    "COMPANY_PROFILE", "SETTINGS", "SETTINGS_MANAGE", "SUBSCRIPTION");
            final Set<String> activeModules = modules; // for lambda

            Set<String> permissions = new java.util.HashSet<>();
            if (user.getRole() != null && user.getRole().getPermissions() != null) {
                user.getRole().getPermissions().stream()
                        .filter(com.project.www.accessmanagement.entity.Permission::getActive)
                        .filter(p -> coreModules.contains(p.getModule()) || activeModules.contains(p.getModule()))
                        .map(com.project.www.accessmanagement.entity.Permission::getPermissionKey)
                        .forEach(permissions::add);
            }
            if (user.getPermissions() != null) {
                user.getPermissions().stream()
                        .filter(com.project.www.accessmanagement.entity.Permission::getActive)
                        .filter(p -> coreModules.contains(p.getModule()) || activeModules.contains(p.getModule()))
                        .map(com.project.www.accessmanagement.entity.Permission::getPermissionKey)
                        .forEach(permissions::add);
            }

            Set<String> finalModules;
            if (user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName())) {
                finalModules = activeModules;
            } else if (user.getModules() != null && !user.getModules().isEmpty()) {
                finalModules = activeModules.stream()
                        .filter(m -> user.getModules().contains(m))
                        .collect(Collectors.toSet());
            } else {
                // If they have no user-level modules, they get no non-core modules unless they
                // have permissions for them
                // But let's just extract modules from their active permissions
                finalModules = permissions.stream()
                        .map(p -> {
                            if (p.startsWith("SUPPORT_TICKETS")) {
                                return "SUPPORT_TICKETS";
                            }
                            return p.split("_")[0];
                        })
                        .filter(activeModules::contains)
                        .collect(Collectors.toSet());
            }

            String token = jwtService.generateToken(user.getEmail(), user.getTenantId(), tenant.getCode());
            System.out.println("DEBUG - Tenant ID: " + tenant.getId() + " - Modules fetched: " + finalModules);
            return new AuthResponse(token, permissions, finalModules,
                    user.getRole() != null ? user.getRole().getName() : null);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (request.getTenantId() != null) {
            // ==========================================
            // Register under existing tenant
            // ==========================================
            Tenant tenant;

            // 1. Query master DB to validate tenant
            String originalTenantCode = TenantContext.getCurrentTenantCode();
            Long originalTenantId = TenantContext.getCurrentTenant();
            try {
                TenantContext.clear();
                tenant = tenantRepository.findById(request.getTenantId())
                        .orElseThrow(() -> new RuntimeException("Tenant not found"));
                if (!tenant.getActive()) {
                    throw new RuntimeException("Tenant is inactive");
                }
            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            // 2. Perform tenant-specific operations
            Role role;
            User user;
            try {
                TenantContext.setCurrentTenant(tenant.getId());
                TenantContext.setCurrentTenantCode(tenant.getCode());

                boolean emailExists = userRepository.existsByEmailAndTenantId(request.getEmail(), tenant.getId());
                if (emailExists) {
                    throw new UserAlreadyExistsException("User already exists under this tenant");
                }

                if (request.getRoleId() != null) {
                    role = roleRepository.findByIdAndTenantId(request.getRoleId(), tenant.getId())
                            .orElseThrow(
                                    () -> new RuntimeException("Role not found or does not belong to this tenant"));
                } else {
                    role = roleRepository.findByNameAndTenantId("SUPER_ADMIN", tenant.getId())
                            .orElseThrow(
                                    () -> new RuntimeException("Default SUPER_ADMIN role not found for this tenant"));
                }

                int currentYear = java.time.LocalDate.now().getYear();
                TenantSettings settings = tenantSettingsRepository.findByTenantId(tenant.getId())
                        .orElseGet(() -> TenantSettings.builder()
                                .tenantId(tenant.getId())
                                .employeeSequence(0L)
                                .leadSequence(0L)
                                .build());
                settings.setEmployeeSequence(settings.getEmployeeSequence() + 1);
                tenantSettingsRepository.save(settings);

                long nextVal = settings.getEmployeeSequence();
                String employeeId = settings.getEmployeeIdFormat() != null
                        && !settings.getEmployeeIdFormat().trim().isEmpty()
                                ? settings.getEmployeeIdFormat()
                                        .replace("{TENANT}", tenant.getCode())
                                        .replace("{YYYY}", String.valueOf(currentYear))
                                        .replace("{SEQ}", String.format("%03d", nextVal))
                                : String.format("EMP-%s-%d-%03d", tenant.getCode(), currentYear, nextVal);

                java.util.List<String[]> defaultPermsInfo = java.util.Arrays.asList(
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
                        new String[] { "COMPANY_PROFILE", "VIEW", "View Company Profile" },
                        new String[] { "COMPANY_PROFILE", "UPDATE", "Update Company Profile" },
                        new String[] { "SETTINGS_MANAGE", "TEMPLATES", "Manage Templates" },
                        new String[] { "SETTINGS_MANAGE", "ONBOARDING", "Manage Onboarding" },
                        new String[] { "SUBSCRIPTION", "MANAGE", "Manage Billing and Subscriptions" },
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
                        new String[] { "MARKETING", "DELETE", "Delete Marketing Campaigns" }, new String[] { "MARKETING", "AJAY_SUMMARY", "Ajay Summary" });

                java.util.List<com.project.www.accessmanagement.entity.Permission> existingPerms = permissionRepository
                        .findAllByTenantId(tenant.getId());
                java.util.Map<String, com.project.www.accessmanagement.entity.Permission> permMap = existingPerms
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(
                                com.project.www.accessmanagement.entity.Permission::getPermissionKey,
                                java.util.function.Function.identity()));

                java.util.List<com.project.www.accessmanagement.entity.Permission> permsToSave = new java.util.ArrayList<>();
                for (String[] permInfo : defaultPermsInfo) {
                    String key = (permInfo[0] + "_" + permInfo[1]).toUpperCase();
                    if (!permMap.containsKey(key)) {
                        permsToSave.add(com.project.www.accessmanagement.entity.Permission.builder()
                                .tenantId(tenant.getId())
                                .module(permInfo[0])
                                .action(permInfo[1])
                                .description(permInfo[2])
                                .active(true)
                                .build());
                    }
                }
                if (!permsToSave.isEmpty()) {
                    permissionRepository.saveAll(permsToSave);
                }

                user = User.builder()
                        .tenantId(tenant.getId())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .active(true)
                        .role(role)
                        .build();
                userRepository.save(user);
                try {
                    TenantContext.clear();
                    globalUserRegistrySyncService.syncUser(user, tenant.getId());
                } finally {
                    TenantContext.setCurrentTenant(tenant.getId());
                    TenantContext.setCurrentTenantCode(tenant.getCode());
                }
            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            Set<String> permissions = role.getPermissions().stream()
                    .map(com.project.www.accessmanagement.entity.Permission::getPermissionKey)
                    .collect(Collectors.toSet());

            Set<String> modules = new HashSet<>();
            String originalContextCode = TenantContext.getCurrentTenantCode();
            try {
                TenantContext.clear();
                modules = tenantModuleRepository.findByTenantIdAndActiveTrue(tenant.getId())
                        .stream().map(com.project.www.tenant.entity.TenantModule::getModuleName)
                        .collect(Collectors.toSet());
            } finally {
                TenantContext.setCurrentTenantCode(originalContextCode);
            }

            String token = jwtService.generateToken(user.getEmail(), user.getTenantId(), tenant.getCode());
            return new AuthResponse(token, permissions, modules, role.getName());

        } else {
            // ==========================================
            // Create a brand new tenant
            // ==========================================
            CreateTenantRequest tenantReq = new CreateTenantRequest();
            tenantReq.setTenantName(request.getTenantName());
            tenantReq.setTenantCode(request.getTenantCode());
            tenantReq.setAdminFirstName(request.getFirstName());
            tenantReq.setAdminLastName(request.getLastName());
            tenantReq.setAdminEmail(request.getEmail());
            tenantReq.setAdminPassword(request.getPassword());

            TenantResponse tenantResponse;
            String originalTenantCode = TenantContext.getCurrentTenantCode();
            Long originalTenantId = TenantContext.getCurrentTenant();

            try {
                // Clear context to ensure TenantService runs entirely on the Master DB where
                // necessary
                TenantContext.clear();
                tenantResponse = tenantService.createTenant(tenantReq);
            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            // Retrieve permissions for newly created admin (they get all initial
            // permissions configured for the tenant)
            Set<String> permissions = new HashSet<>();
            try {
                TenantContext.setCurrentTenant(tenantResponse.getId());
                TenantContext.setCurrentTenantCode(tenantResponse.getCode());
                User adminUser = userRepository
                        .findFirstByEmailAndTenantId(tenantResponse.getAdminEmail(), tenantResponse.getId())
                        .orElse(null);
                if (adminUser != null && adminUser.getRole() != null) {
                    permissions = adminUser.getRole().getPermissions().stream()
                            .map(com.project.www.accessmanagement.entity.Permission::getPermissionKey)
                            .collect(Collectors.toSet());
                }
            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            // Generate token immediately for the newly registered admin user
            String token = jwtService.generateToken(tenantResponse.getAdminEmail(), tenantResponse.getId(),
                    tenantResponse.getCode());

            Set<String> modules = new HashSet<>();
            String originalContextCode = TenantContext.getCurrentTenantCode();
            try {
                TenantContext.clear();
                modules = tenantModuleRepository.findByTenantIdAndActiveTrue(tenantResponse.getId())
                        .stream().map(com.project.www.tenant.entity.TenantModule::getModuleName)
                        .collect(Collectors.toSet());
            } finally {
                TenantContext.setCurrentTenantCode(originalContextCode);
            }

            return new AuthResponse(token, permissions, modules, "SUPER_ADMIN");
        }
    }
}
