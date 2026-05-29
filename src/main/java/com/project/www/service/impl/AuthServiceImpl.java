package com.project.www.service.impl;

import com.project.www.dto.AuthResponse;
import com.project.www.dto.CreateTenantRequest;
import com.project.www.dto.LoginRequest;
import com.project.www.dto.RegisterRequest;
import com.project.www.dto.TenantResponse;
import com.project.www.entity.Role;
import com.project.www.entity.Tenant;
import com.project.www.entity.User;
import com.project.www.exception.UserAlreadyExistsException;
import com.project.www.entity.TenantSettings;
import com.project.www.repository.TenantSettingsRepository;
import com.project.www.repository.RoleRepository;
import com.project.www.repository.TenantRepository;
import com.project.www.repository.UserRepository;
import com.project.www.security.JwtService;
import com.project.www.service.AuthService;
import com.project.www.service.TenantService;
import com.project.www.repository.TenantModuleRepository;
import com.project.www.entity.TenantModule;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
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
    private final TenantService tenantService;
    private final TenantModuleRepository tenantModuleRepository;
    private final com.project.www.repository.PermissionRepository permissionRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        String originalTenantCode = TenantContext.getCurrentTenantCode();
        Long originalTenantId = TenantContext.getCurrentTenant();
        Tenant tenant = null;
        
        try {
            TenantContext.clear(); // Ensure we query the master database
            if (request.getTenantId() != null) {
                tenant = tenantRepository.findById(request.getTenantId()).orElse(null);
            }

            if (tenant == null) {
                String tenantCode = tenantResolver.resolveTenantCode(servletRequest);
                if (tenantCode != null) {
                    tenant = tenantRepository.findByCode(tenantCode).orElse(null);
                }
            }
            
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

        if (tenant == null) {
            throw new RuntimeException("Tenant not found or not specified");
        }

        if (!tenant.getActive()) {
            throw new RuntimeException("Tenant is inactive");
        }

        // Set tenant context for authentication loading
        TenantContext.setCurrentTenant(tenant.getId());
        TenantContext.setCurrentTenantCode(tenant.getCode());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenant.getId())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

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

            java.util.List<String> coreModules = java.util.Arrays.asList("USER", "ROLE", "TENANT", "PERMISSION");
            final Set<String> activeModules = modules; // for lambda

            Set<String> permissions = user.getRole().getPermissions().stream()
                    .filter(p -> coreModules.contains(p.getModule()) || activeModules.contains(p.getModule()))
                    .map(com.project.www.entity.Permission::getPermissionKey)
                    .collect(Collectors.toSet());

            String token = jwtService.generateToken(user.getEmail(), user.getTenantId(), tenant.getCode());
            return new AuthResponse(token, permissions, modules);
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
                            .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));
                } else {
                    role = roleRepository.findByNameAndTenantId("SUPER_ADMIN", tenant.getId())
                            .orElseThrow(() -> new RuntimeException("Default SUPER_ADMIN role not found for this tenant"));
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
                String employeeId = settings.getEmployeeIdFormat() != null && !settings.getEmployeeIdFormat().trim().isEmpty()
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

                java.util.List<com.project.www.entity.Permission> existingPerms = permissionRepository.findAllByTenantId(tenant.getId());
                java.util.Map<String, com.project.www.entity.Permission> permMap = existingPerms.stream()
                        .collect(java.util.stream.Collectors.toMap(com.project.www.entity.Permission::getPermissionKey, java.util.function.Function.identity()));

                java.util.List<com.project.www.entity.Permission> permsToSave = new java.util.ArrayList<>();
                for (String[] permInfo : defaultPermsInfo) {
                    String key = (permInfo[0] + "_" + permInfo[1]).toUpperCase();
                    if (!permMap.containsKey(key)) {
                        permsToSave.add(com.project.www.entity.Permission.builder()
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
                        .employeeId(employeeId)
                        .active(true)
                        .role(role)
                        .build();

                userRepository.save(user);

            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            Set<String> permissions = role.getPermissions().stream()
                    .map(com.project.www.entity.Permission::getPermissionKey)
                    .collect(Collectors.toSet());

            Set<String> modules = new HashSet<>();
            String originalContextCode = TenantContext.getCurrentTenantCode();
            try {
                TenantContext.clear();
                modules = tenantModuleRepository.findByTenantIdAndActiveTrue(tenant.getId())
                        .stream().map(com.project.www.entity.TenantModule::getModuleName)
                        .collect(Collectors.toSet());
            } finally {
                TenantContext.setCurrentTenantCode(originalContextCode);
            }

            String token = jwtService.generateToken(user.getEmail(), user.getTenantId(), tenant.getCode());
            return new AuthResponse(token, permissions, modules);

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
                // Clear context to ensure TenantService runs entirely on the Master DB where necessary
                TenantContext.clear();
                tenantResponse = tenantService.createTenant(tenantReq);
            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            // Retrieve permissions for newly created admin (they get all initial permissions configured for the tenant)
            Set<String> permissions = new HashSet<>();
            try {
                TenantContext.setCurrentTenant(tenantResponse.getId());
                TenantContext.setCurrentTenantCode(tenantResponse.getCode());
                User adminUser = userRepository.findByEmailAndTenantId(tenantResponse.getAdminEmail(), tenantResponse.getId())
                        .orElse(null);
                if (adminUser != null && adminUser.getRole() != null) {
                    permissions = adminUser.getRole().getPermissions().stream()
                            .map(com.project.www.entity.Permission::getPermissionKey)
                            .collect(Collectors.toSet());
                }
            } finally {
                TenantContext.setCurrentTenant(originalTenantId);
                TenantContext.setCurrentTenantCode(originalTenantCode);
            }

            // Generate token immediately for the newly registered admin user
            String token = jwtService.generateToken(tenantResponse.getAdminEmail(), tenantResponse.getId(), tenantResponse.getCode());
            
            Set<String> modules = new HashSet<>();
            String originalContextCode = TenantContext.getCurrentTenantCode();
            try {
                TenantContext.clear();
                modules = tenantModuleRepository.findByTenantIdAndActiveTrue(tenantResponse.getId())
                        .stream().map(com.project.www.entity.TenantModule::getModuleName)
                        .collect(Collectors.toSet());
            } finally {
                TenantContext.setCurrentTenantCode(originalContextCode);
            }
            
            return new AuthResponse(token, permissions, modules);
        }
    }
}