package com.project.www.accessmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.accessmanagement.entity.Permission;
import com.project.www.accessmanagement.entity.Role;
import com.project.www.accessmanagement.entity.RoleHierarchy;
import com.project.www.accessmanagement.repository.PermissionRepository;
import com.project.www.accessmanagement.repository.RoleHierarchyRepository;
import com.project.www.accessmanagement.repository.RoleRepository;
import com.project.www.service.EmailService;
import com.project.www.tenant.entity.IdFormatSetting;
import com.project.www.tenant.entity.OfficeLocation;
import com.project.www.tenant.repository.IdFormatSettingRepository;
import com.project.www.tenant.repository.OfficeLocationRepository;
import com.project.www.tenant.repository.TenantSettingsRepository;
import com.project.www.accessmanagement.dto.CreateUserRequest;
import com.project.www.accessmanagement.dto.ResetPasswordRequest;
import com.project.www.accessmanagement.dto.UserResponse;
import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.entity.UserReporting;
import com.project.www.accessmanagement.repository.UserReportingRepository;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.accessmanagement.service.UserService;
import com.project.www.util.TenantContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final OfficeLocationRepository officeLocationRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final IdFormatSettingRepository idFormatSettingRepository;
    private final com.project.www.accessmanagement.service.RoleExtraFieldService roleExtraFieldService;
    private final RoleHierarchyRepository roleHierarchyRepository;
    private final UserReportingRepository userReportingRepository;
    private final PermissionRepository permissionRepository;
    private final com.project.www.tenant.repository.EmployeeTypeRepository employeeTypeRepository;
    private final com.project.www.tenant.repository.DesignationRepository designationRepository;
    private final com.project.www.tenant.repository.WorkModeRepository workModeRepository;
    private final com.project.www.tenant.repository.BusinessEntityRepository businessEntityRepository;
    private final com.project.www.tenant.repository.DepartmentRepository departmentRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final com.project.www.accessmanagement.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;
    private final com.project.www.accessmanagement.repository.GlobalUserRegistryRepository globalUserRegistryRepository;
    private final com.project.www.tenant.repository.TenantRepository tenantRepository;
    private final com.project.www.tenant.repository.TenantModuleRepository tenantModuleRepository;
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void createUser(CreateUserRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        boolean emailExists = userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId);
        if (emailExists) {
            throw new RuntimeException("User email already exists under this tenant");
        }
        boolean globalEmailExists;
        String ogCode = TenantContext.getCurrentTenantCode();
        Long ogId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            globalEmailExists = globalUserRegistryRepository.existsByEmailAndTenantCode(request.getEmail(), ogCode);
        } finally {
            TenantContext.setCurrentTenant(ogId);
            TenantContext.setCurrentTenantCode(ogCode);
        }

        if (globalEmailExists) {
            throw new RuntimeException("Email already exists in this workspace.");
        }

        java.util.Set<Role> roles = new java.util.HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long rId : request.getRoleIds()) {
                Role r = roleRepository.findByIdAndTenantId(rId, tenantId)
                        .orElseThrow(
                                () -> new RuntimeException("Role not found or does not belong to this tenant: " + rId));
                if (!r.getActive()) {
                    throw new RuntimeException("Assigned role is disabled: " + r.getName());
                }
                roles.add(r);
            }
        }

        Role role;
        if (request.getRoleId() != null) {
            role = roleRepository.findByIdAndTenantId(request.getRoleId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));
        } else if (request.getRoleCode() != null && !request.getRoleCode().trim().isEmpty()) {
            // Find by name or code
            role = roleRepository.findByCodeAndTenantId(request.getRoleCode(), tenantId)
                    .or(() -> roleRepository.findByNameAndTenantId(request.getRoleCode(), tenantId))
                    .orElseThrow(() -> new RuntimeException(
                            "Role not found or does not belong to this tenant: " + request.getRoleCode()));
        } else if (!roles.isEmpty()) {
            role = roles.iterator().next();
        } else {
            throw new RuntimeException("A valid role must be specified when creating a user");
        }

        if (!role.getActive()) {
            throw new RuntimeException("Assigned role is disabled");
        }
        roles.add(role);

        // Get tenant code from context directly since querying tenantRepository
        // on a tenant-specific connection will fail (tenants is a master db table).
        String tenantCode = TenantContext.getCurrentTenantCode();
        if (tenantCode == null) {
            throw new RuntimeException("No tenant code found in context");
        }

        int currentYear = java.time.LocalDate.now().getYear();

        OfficeLocation location = null;

        if (!"LEAD".equalsIgnoreCase(role.getName())) {
            if (request.getProfileData() != null) {
                if (request.getProfileData().get("officeLocationId") != null) {
                    Long locId = Long.valueOf(request.getProfileData().get("officeLocationId").toString());
                    location = officeLocationRepository.findById(locId).orElse(null);
                }
            }
        }

        java.util.Set<String> modules = new java.util.HashSet<>();
        if (request.getModules() != null) {
            modules.addAll(request.getModules());
        }

        java.util.Set<Permission> permissions = new java.util.HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(request.getPermissionIds()));
        }

        com.project.www.tenant.entity.EmployeeType empType = null;
        if (request.getEmployeeTypeId() != null) {
            empType = employeeTypeRepository.findByIdAndTenantId(request.getEmployeeTypeId(), tenantId).orElse(null);
        }

        com.project.www.tenant.entity.Designation desig = null;
        if (request.getDesignationId() != null) {
            desig = designationRepository.findByIdAndTenantId(request.getDesignationId(), tenantId).orElse(null);
        }

        com.project.www.tenant.entity.WorkMode mode = null;
        if (request.getWorkModeId() != null) {
            mode = workModeRepository.findByIdAndTenantId(request.getWorkModeId(), tenantId).orElse(null);
        }

        java.util.Set<com.project.www.tenant.entity.BusinessEntity> entities = new java.util.HashSet<>();
        if (request.getEntityIds() != null && !request.getEntityIds().isEmpty()) {
            entities.addAll(businessEntityRepository.findAllById(request.getEntityIds()));
        }

        java.util.Set<com.project.www.tenant.entity.Department> departments = new java.util.HashSet<>();
        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            departments.addAll(departmentRepository.findAllById(request.getDepartmentIds()));
        }

        User user = User.builder()
                .tenantId(tenantId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .role(role)
                .roles(roles)
                .permissions(permissions)
                .modules(modules)
                .assignedOffice(location)
                .entities(entities)
                .departments(departments)
                .employeeId(request.getEmployeeId())
                .dateOfBirth(request.getDateOfBirth())
                .joiningDate(request.getJoiningDate())
                .employeeType(empType)
                .designation(desig)
                .workMode(mode)
                .build();

        user = userRepository.save(user);
        globalUserRegistrySyncService.syncUser(user, tenantId);
        roleExtraFieldService.saveUserExtraFieldValues(user, request.getProfileData());

        if (request.getSupervisorUserId() != null && request.getSupervisorUserId() > 0) {
            User supervisor = userRepository.findByIdAndTenantId(request.getSupervisorUserId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Supervisor user not found with ID: " + request.getSupervisorUserId()));
            UserReporting reporting = UserReporting.builder()
                    .tenantId(tenantId)
                    .user(user)
                    .supervisorUser(supervisor)
                    .build();
            userReportingRepository.save(reporting);
        }

        // Generate ID based on role (LEAD, EMPLOYEE, etc)
        String entityType = role.getName().toUpperCase();
        if ("LEAD".equals(entityType) || "EMPLOYEE".equals(entityType)) {
            String defaultPrefix = "LEAD".equals(entityType) ? "LEA" : "EMP";
            
            IdFormatSetting format = idFormatSettingRepository.findByTenantIdAndEntityType(tenantId, entityType)
                    .orElseGet(() -> IdFormatSetting.builder()
                            .tenantId(tenantId)
                            .entityType(entityType)
                            .prefix(defaultPrefix)
                            .paddingLength(7)
                            .nextSequence(1L)
                            .includeYear(false)
                            .active(true)
                            .build());

            long nextVal = format.getNextSequence();
            String generatedId;
            if (Boolean.TRUE.equals(format.getIncludeYear())) {
                generatedId = format.getPrefix() + currentYear
                        + String.format("%0" + format.getPaddingLength() + "d", nextVal);
            } else {
                generatedId = format.getPrefix() + String.format("%0" + format.getPaddingLength() + "d", nextVal);
            }

            format.setNextSequence(nextVal + 1);
            idFormatSettingRepository.save(format);
            
            // Set it on user and save again
            user.setEmployeeId(generatedId);
            userRepository.save(user);
        }

        // Send email to user
        try {
            String loginId = user.getEmail();
            String loginUrl = frontendUrl + "/login";

            // Try to find custom domain
            final String[] customDomainRef = { null };
            Long originalTenant = TenantContext.getCurrentTenant();
            String originalCode = TenantContext.getCurrentTenantCode();
            try {
                TenantContext.clear();
                org.springframework.transaction.support.TransactionTemplate template = new org.springframework.transaction.support.TransactionTemplate(
                        transactionManager);
                template.setPropagationBehavior(
                        org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                template.setReadOnly(true);
                template.executeWithoutResult(status -> {
                    com.project.www.tenant.entity.Tenant t = tenantRepository.findById(tenantId).orElse(null);
                    if (t != null && t.getDomain() != null && !t.getDomain().trim().isEmpty()) {
                        customDomainRef[0] = t.getDomain();
                    }
                });

                if (customDomainRef[0] != null) {
                    loginUrl = "http://" + customDomainRef[0] + ":5173/login";
                }
            } finally {
                TenantContext.setCurrentTenant(originalTenant);
                TenantContext.setCurrentTenantCode(originalCode);
            }

            emailService.sendCredentialsEmail(user.getEmail(), user.getFirstName(), loginId, request.getPassword(),
                    loginUrl, tenantCode);
        } catch (Exception e) {
            // Log it, but don't fail the user creation
            System.err.println("Failed to send credentials email to: " + user.getEmail());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByTenant(Long tenantId) {
        Long currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        if (!currentTenantId.equals(tenantId)) {
            throw new RuntimeException("Access denied: Tenant mismatch");
        }

        List<User> users = userRepository.findAllByTenantId(tenantId);
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }

        Long currentUserId = com.project.www.security.UserContext.getCurrentUserId();
        User currentUser = userRepository.findByIdAndTenantId(currentUserId, tenantId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<User> users;

        // If Super Admin, return all users
        if (currentUser.getRole() != null && ("SUPER_ADMIN".equals(currentUser.getRole().getName()) || "SYSTEM_SUPER_ADMIN".equals(currentUser.getRole().getName()))) {
            users = userRepository.findAllByTenantId(tenantId);
        } else {
            // Otherwise, recursively fetch all downstream reports
            users = new java.util.ArrayList<>();
            users.add(currentUser);
            
            java.util.Set<Long> seenIds = new java.util.HashSet<>();
            seenIds.add(currentUser.getId());
            
            java.util.Queue<Long> supervisorIds = new java.util.LinkedList<>();
            supervisorIds.add(currentUser.getId());
            
            while (!supervisorIds.isEmpty()) {
                Long currentSupId = supervisorIds.poll();
                List<UserReporting> reports = userReportingRepository.findAllBySupervisorUserIdAndTenantId(currentSupId, tenantId);
                for (UserReporting report : reports) {
                    User subUser = report.getUser();
                    if (subUser != null && !seenIds.contains(subUser.getId())) {
                        seenIds.add(subUser.getId());
                        users.add(subUser);
                        supervisorIds.add(subUser.getId());
                    }
                }
            }
        }

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found in this tenant"));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }

        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found in this tenant"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getEmployeeId() != null) {
            user.setEmployeeId(request.getEmployeeId());
        }
        user.setDateOfBirth(request.getDateOfBirth());
        user.setJoiningDate(request.getJoiningDate());

        if (request.getEmployeeTypeId() != null) {
            user.setEmployeeType(employeeTypeRepository.findByIdAndTenantId(request.getEmployeeTypeId(), tenantId).orElse(null));
        } else {
            user.setEmployeeType(null);
        }

        if (request.getDesignationId() != null) {
            user.setDesignation(designationRepository.findByIdAndTenantId(request.getDesignationId(), tenantId).orElse(null));
        } else {
            user.setDesignation(null);
        }

        if (request.getWorkModeId() != null) {
            user.setWorkMode(workModeRepository.findByIdAndTenantId(request.getWorkModeId(), tenantId).orElse(null));
        } else {
            user.setWorkMode(null);
        }

        java.util.Set<Role> roles = new java.util.HashSet<>();
        Role primaryRole = null;
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long rId : request.getRoleIds()) {
                Role r = roleRepository.findByIdAndTenantId(rId, tenantId)
                        .orElseThrow(
                                () -> new RuntimeException("Role not found or does not belong to this tenant: " + rId));
                roles.add(r);
            }
            primaryRole = roles.iterator().next();
            user.setRoles(roles);
            user.setRole(primaryRole);
        } else if (request.getRoleId() != null) {
            primaryRole = roleRepository.findByIdAndTenantId(request.getRoleId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));
            roles.add(primaryRole);
            user.setRoles(roles);
            user.setRole(primaryRole);
        } else if (request.getRoleCode() != null) {
            primaryRole = roleRepository.findByCodeAndTenantId(request.getRoleCode(), tenantId)
                    .or(() -> roleRepository.findByNameAndTenantId(request.getRoleCode(), tenantId))
                    .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));
            roles.add(primaryRole);
            user.setRoles(roles);
            user.setRole(primaryRole);
        }

        if (request.getModules() != null) {
            user.setModules(new java.util.HashSet<>(request.getModules()));
        }
        if (request.getPermissionIds() != null) {
            user.setPermissions(new java.util.HashSet<>(permissionRepository.findAllById(request.getPermissionIds())));
        }

        if (request.getEntityIds() != null) {
            user.setEntities(new java.util.HashSet<>(businessEntityRepository.findAllById(request.getEntityIds())));
        }
        if (request.getDepartmentIds() != null) {
            user.setDepartments(new java.util.HashSet<>(departmentRepository.findAllById(request.getDepartmentIds())));
        }

        // --- SUPER ADMIN PROTECTION: PREVENT DEMOTION ---
        boolean wasSuperAdmin = user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName());
        boolean isNowSuperAdmin = primaryRole != null && "SUPER_ADMIN".equals(primaryRole.getName());

        if (wasSuperAdmin && !isNowSuperAdmin) {
            // Cannot demote the platform admin
            if (user.getTenantId() == 1L && "admin@lms.com".equalsIgnoreCase(user.getEmail())) {
                throw new RuntimeException("System Protected Account cannot be demoted.");
            }
            // Cannot demote the last active tenant super admin
            long activeSuperAdmins = userRepository.countByRoleNameAndTenantIdAndActiveTrue("SUPER_ADMIN", tenantId);
            if (activeSuperAdmins <= 1) {
                throw new RuntimeException("At least one active Super Admin must exist for this tenant.");
            }
        }
        // ------------------------------------------------

        User updated = userRepository.save(user);
        globalUserRegistrySyncService.syncUser(updated, tenantId);
        roleExtraFieldService.saveUserExtraFieldValues(updated, request.getProfileData());

        if (request.getSupervisorUserId() != null && request.getSupervisorUserId() > 0) {
            User supervisor = userRepository.findByIdAndTenantId(request.getSupervisorUserId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Supervisor user not found with ID: " + request.getSupervisorUserId()));
                    
            java.util.List<UserReporting> existingList = userReportingRepository.findAllByUserIdAndTenantId(updated.getId(), tenantId);
            if (existingList.isEmpty()) {
                UserReporting reporting = UserReporting.builder()
                        .tenantId(tenantId)
                        .user(updated)
                        .supervisorUser(supervisor)
                        .build();
                userReportingRepository.save(reporting);
            } else {
                UserReporting reporting = existingList.get(0);
                reporting.setSupervisorUser(supervisor);
                userReportingRepository.save(reporting);
                
                // Clean up any extra records if they exist
                if (existingList.size() > 1) {
                    for (int i = 1; i < existingList.size(); i++) {
                        userReportingRepository.delete(existingList.get(i));
                    }
                }
            }
        } else {
            userReportingRepository.deleteAllByUserIdAndTenantId(updated.getId(), tenantId);
        }

        return mapToResponse(updated);
    }

    /**
     * Soft-deactivates a user. Physical deletion is not permitted to preserve audit
     * history
     * and reporting hierarchy integrity.
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        deactivateUser(id);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found in this tenant"));

        // --- SUPER ADMIN PROTECTION: PREVENT DEACTIVATION ---
        if (user.getTenantId() == 1L && "admin@lms.com".equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("System Protected Account cannot be deactivated.");
        }

        if (user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName())) {
            long activeSuperAdmins = userRepository.countByRoleNameAndTenantIdAndActiveTrue("SUPER_ADMIN", tenantId);
            if (activeSuperAdmins <= 1) {
                throw new RuntimeException("At least one active Super Admin must exist for this tenant.");
            }
        }
        // ---------------------------------------------------

        user.setActive(false);
        userRepository.save(user);
        globalUserRegistrySyncService.syncUser(user, tenantId);
    }

    @Override
    @Transactional
    public void toggleUserActiveStatus(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found in this tenant"));

        // --- SUPER ADMIN PROTECTION: PREVENT DEACTIVATION ---
        if (user.getActive()) {
            if (user.getTenantId() == 1L && "admin@lms.com".equalsIgnoreCase(user.getEmail())) {
                throw new RuntimeException("System Protected Account cannot be deactivated.");
            }

            if (user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName())) {
                long activeSuperAdmins = userRepository.countByRoleNameAndTenantIdAndActiveTrue("SUPER_ADMIN",
                        tenantId);
                if (activeSuperAdmins <= 1) {
                    throw new RuntimeException("At least one active Super Admin must exist for this tenant.");
                }
            }
        }
        // ---------------------------------------------------

        user.setActive(!user.getActive());
        userRepository.save(user);
        globalUserRegistrySyncService.syncUser(user, tenantId);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }

        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found or does not belong to this tenant"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        Long tenantId = TenantContext.getCurrentTenant();
        java.util.Map<String, Object> extraFields = roleExtraFieldService.getUserExtraFieldValues(user.getId());
        java.util.Map<String, Object> mergedProfile = new java.util.LinkedHashMap<>(extraFields);
        if (user.getAssignedOffice() != null) {
            mergedProfile.put("officeLocationId", user.getAssignedOffice().getId());
        }

        java.util.List<Long> roleIds = new java.util.ArrayList<>();
        java.util.List<String> roleNames = new java.util.ArrayList<>();
        if (user.getRole() != null) {
            roleIds.add(user.getRole().getId());
            roleNames.add(user.getRole().getName());
        }
        if (user.getRoles() != null) {
            for (Role r : user.getRoles()) {
                if (!roleIds.contains(r.getId())) {
                    roleIds.add(r.getId());
                    roleNames.add(r.getName());
                }
            }
        }

        java.util.List<String> userModules = new java.util.ArrayList<>();
        java.util.List<String> userPermissions = new java.util.ArrayList<>();
        java.util.List<Long> userPermissionIds = new java.util.ArrayList<>();

        if (user.getRole() != null && ("SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()) || "SYSTEM_SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()))) {
            if (tenantId != null) {
                String originalTenantCode = TenantContext.getCurrentTenantCode();
                Long originalTenantId = TenantContext.getCurrentTenant();
                
                // Fetch modules from master database for both admin types
                try {
                    TenantContext.clear();
                    org.springframework.transaction.support.TransactionTemplate tt = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
                    tt.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    
                    userModules = tt.execute(status -> tenantModuleRepository.findByTenantIdAndActiveTrue(tenantId).stream()
                            .map(com.project.www.tenant.entity.TenantModule::getModuleName)
                            .collect(Collectors.toList()));
                } finally {
                    TenantContext.setCurrentTenant(originalTenantId);
                    TenantContext.setCurrentTenantCode(originalTenantCode);
                }

                // Fetch permissions (from master DB for SYSTEM_SUPER_ADMIN, from tenant DB for SUPER_ADMIN)
                if ("SYSTEM_SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                    try {
                        TenantContext.clear();
                        org.springframework.transaction.support.TransactionTemplate tt = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
                        tt.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        
                        java.util.List<Permission> allPerms = tt.execute(status -> permissionRepository.findAllByTenantId(tenantId).stream()
                                .filter(Permission::getActive)
                                .collect(Collectors.toList()));
                        if (allPerms != null) {
                            userPermissions = allPerms.stream().map(Permission::getPermissionKey).collect(Collectors.toList());
                            userPermissionIds = allPerms.stream().map(Permission::getId).collect(Collectors.toList());
                        }
                    } finally {
                        TenantContext.setCurrentTenant(originalTenantId);
                        TenantContext.setCurrentTenantCode(originalTenantCode);
                    }
                } else {
                    // SUPER_ADMIN (Tenant Database)
                    java.util.List<Permission> allPerms = permissionRepository.findAllByTenantId(tenantId).stream()
                            .filter(Permission::getActive)
                            .collect(Collectors.toList());
                    if (allPerms != null) {
                        userPermissions = allPerms.stream().map(Permission::getPermissionKey).collect(Collectors.toList());
                        userPermissionIds = allPerms.stream().map(Permission::getId).collect(Collectors.toList());
                    }
                }
            }
        } else {
            if (user.getModules() != null) {
                userModules.addAll(user.getModules());
            }
            if (user.getPermissions() != null) {
                userPermissions = user.getPermissions().stream().map(Permission::getPermissionKey).collect(Collectors.toList());
                userPermissionIds = user.getPermissions().stream().map(Permission::getId).collect(Collectors.toList());
            }
        }
        
        // Ensure core system modules are always enabled so the frontend displays settings/admin sections
        if (!userModules.contains("ADMIN")) {
            userModules.add("ADMIN");
        }
        if (!userModules.contains("EMPLOYEE")) {
            userModules.add("EMPLOYEE");
        }
        if (!userModules.contains("SETTINGS")) {
            userModules.add("SETTINGS");
        }

        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .profileData(mergedProfile)
                .active(user.getActive())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .roleIds(roleIds)
                .roleNames(roleNames)
                .supervisorUserId(tenantId != null
                        ? userReportingRepository.findAllByUserIdAndTenantId(user.getId(), tenantId).stream()
                                .findFirst().map(ur -> ur.getSupervisorUser().getId()).orElse(null)
                        : null)
                .supervisorName(tenantId != null ? userReportingRepository
                        .findAllByUserIdAndTenantId(user.getId(), tenantId).stream().findFirst()
                        .map(ur -> ur.getSupervisorUser().getFirstName() + " " + ur.getSupervisorUser().getLastName())
                        .orElse(null) : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .modules(userModules)
                .permissions(userPermissions)
                .permissionIds(userPermissionIds)
                .employeeId(user.getEmployeeId())
                .dateOfBirth(user.getDateOfBirth())
                .joiningDate(user.getJoiningDate())
                .employeeTypeId(user.getEmployeeType() != null ? user.getEmployeeType().getId() : null)
                .employeeTypeName(user.getEmployeeType() != null ? user.getEmployeeType().getName() : null)
                .designationId(user.getDesignation() != null ? user.getDesignation().getId() : null)
                .designationName(user.getDesignation() != null ? user.getDesignation().getName() : null)
                .workModeId(user.getWorkMode() != null ? user.getWorkMode().getId() : null)
                .workModeName(user.getWorkMode() != null ? user.getWorkMode().getName() : null)
                .entityIds(user.getEntities() != null
                        ? user.getEntities().stream().map(com.project.www.tenant.entity.BusinessEntity::getId).collect(Collectors.toList())
                        : new java.util.ArrayList<>())
                .entityNames(user.getEntities() != null
                        ? user.getEntities().stream().map(com.project.www.tenant.entity.BusinessEntity::getCompanyName).collect(Collectors.toList())
                        : new java.util.ArrayList<>())
                .departmentIds(user.getDepartments() != null
                        ? user.getDepartments().stream().map(com.project.www.tenant.entity.Department::getId).collect(Collectors.toList())
                        : new java.util.ArrayList<>())
                .departmentNames(user.getDepartments() != null
                        ? user.getDepartments().stream().map(com.project.www.tenant.entity.Department::getDeptName).collect(Collectors.toList())
                        : new java.util.ArrayList<>())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.project.www.dto.SupervisorResponse> getSupervisorsForRole(Long roleId, String roleCode) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }

        Long targetRoleId = roleId;
        if (targetRoleId == null && roleCode != null && !roleCode.trim().isEmpty()) {
            Role roleObj = roleRepository.findByCodeAndTenantId(roleCode, tenantId)
                    .or(() -> roleRepository.findByNameAndTenantId(roleCode, tenantId))
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
            targetRoleId = roleObj.getId();
        }

        if (targetRoleId == null) {
            return java.util.Collections.emptyList();
        }

        List<RoleHierarchy> hierarchies = roleHierarchyRepository.findAllByRoleIdAndTenantId(targetRoleId, tenantId);
        if (hierarchies.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<Long> reportsToRoleIds = hierarchies.stream()
                .map(h -> h.getReportsToRole().getId())
                .collect(Collectors.toList());

        List<User> users = userRepository.findAllByTenantId(tenantId);
        List<com.project.www.dto.SupervisorResponse> supervisors = new java.util.ArrayList<>();
        for (User u : users) {
            if (!u.getActive()) {
                continue;
            }
            boolean matchesRole = false;
            if (u.getRole() != null && reportsToRoleIds.contains(u.getRole().getId())) {
                matchesRole = true;
            } else if (u.getRoles() != null) {
                for (Role r : u.getRoles()) {
                    if (reportsToRoleIds.contains(r.getId())) {
                        matchesRole = true;
                        break;
                    }
                }
            }

            if (matchesRole) {
                supervisors.add(com.project.www.dto.SupervisorResponse.builder()
                        .id(u.getId())
                        .name(u.getFirstName() + " " + u.getLastName())
                        .build());
            }
        }

        return supervisors;
    }

    @Override
    @Transactional(readOnly = true)
    public com.project.www.accessmanagement.dto.AccessScopeResponse getAccessScope() {
        Long currentUserId = com.project.www.security.UserContext.getCurrentUserId();
        Long tenantId = TenantContext.getCurrentTenant();

        if (currentUserId == null || tenantId == null) {
            throw new RuntimeException("Unauthorized or no active tenant context found");
        }

        User user = userRepository.findByIdAndTenantId(currentUserId, tenantId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.project.www.accessmanagement.dto.AccessScopeResponse.AccessScopeResponseBuilder builder = 
                com.project.www.accessmanagement.dto.AccessScopeResponse.builder()
                .loggedInUserId(user.getId())
                .role(user.getRole() != null ? user.getRole().getName() : null);

        if (user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName())) {
            builder.canViewAll(true);
            builder.accessibleEmployeeIds(new java.util.ArrayList<>());
            return builder.build();
        }

        builder.canViewAll(false);
        java.util.Set<String> accessibleEmployeeIds = new java.util.HashSet<>();
        java.util.Set<Long> visitedUserIds = new java.util.HashSet<>();

        // Add self
        if (user.getEmployeeId() != null) {
            accessibleEmployeeIds.add(user.getEmployeeId());
        }

        // Recursively find all downstream reports
        collectAccessibleEmployeeIds(user.getId(), tenantId, accessibleEmployeeIds, visitedUserIds);

        builder.accessibleEmployeeIds(new java.util.ArrayList<>(accessibleEmployeeIds));
        return builder.build();
    }

    private void collectAccessibleEmployeeIds(Long supervisorId, Long tenantId, java.util.Set<String> employeeIds, java.util.Set<Long> visitedUserIds) {
        if (visitedUserIds.contains(supervisorId)) return;
        visitedUserIds.add(supervisorId);

        List<UserReporting> reports = userReportingRepository.findAllBySupervisorUserIdAndTenantId(supervisorId, tenantId);
        for (UserReporting report : reports) {
            User subordinate = report.getUser();
            if (subordinate.getEmployeeId() != null) {
                employeeIds.add(subordinate.getEmployeeId());
            }
            collectAccessibleEmployeeIds(subordinate.getId(), tenantId, employeeIds, visitedUserIds);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getDirectReports(Long supervisorId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No active tenant context found");
        }

        userRepository.findByIdAndTenantId(supervisorId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        List<UserReporting> reports = userReportingRepository.findAllBySupervisorUserIdAndTenantId(supervisorId, tenantId);

        return reports.stream()
                .map(UserReporting::getUser)
                .filter(User::getActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
