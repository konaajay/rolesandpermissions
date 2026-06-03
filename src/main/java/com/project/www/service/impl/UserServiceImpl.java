package com.project.www.service.impl;

import com.project.www.dto.CreateUserRequest;
import com.project.www.dto.UserResponse;
import com.project.www.dto.ResetPasswordRequest;
import com.project.www.entity.*;
import com.project.www.repository.*;
import com.project.www.service.UserService;
import com.project.www.service.EmailService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LeadProfileRepository leadProfileRepository;
    private final OfficeLocationRepository officeLocationRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final IdFormatSettingRepository idFormatSettingRepository;
    private final com.project.www.service.RoleExtraFieldService roleExtraFieldService;
    private final RoleHierarchyRepository roleHierarchyRepository;
    private final UserReportingRepository userReportingRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final com.project.www.service.GlobalUserRegistrySyncService globalUserRegistrySyncService;
    private final com.project.www.repository.GlobalUserRegistryRepository globalUserRegistryRepository;
    private final com.project.www.repository.TenantRepository tenantRepository;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void createUser(CreateUserRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
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
            globalEmailExists = globalUserRegistrySyncService.existsByEmail(request.getEmail());
        } finally {
            TenantContext.setCurrentTenant(ogId);
            TenantContext.setCurrentTenantCode(ogCode);
        }

        if (globalEmailExists) {
            throw new RuntimeException("Email already exists in another workspace. Please use a unique email.");
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
                .assignedOffice(location)
                .build();

        user = userRepository.save(user);
        globalUserRegistrySyncService.syncUser(user, tenantId);
        roleExtraFieldService.saveUserExtraFieldValues(user, request.getProfileData());

        if (request.getSupervisorUserId() != null) {
            User supervisor = userRepository.findByIdAndTenantId(request.getSupervisorUserId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Supervisor user not found"));
            UserReporting reporting = UserReporting.builder()
                    .tenantId(tenantId)
                    .user(user)
                    .supervisorUser(supervisor)
                    .build();
            userReportingRepository.save(reporting);
        }

        // Create profile based on role
        if ("LEAD".equalsIgnoreCase(role.getName())) {
            IdFormatSetting leadFormat = idFormatSettingRepository.findByTenantIdAndEntityType(tenantId, "LEAD")
                    .orElseGet(() -> IdFormatSetting.builder()
                            .tenantId(tenantId)
                            .entityType("LEAD")
                            .prefix("LEA")
                            .paddingLength(7)
                            .nextSequence(1L)
                            .includeYear(false)
                            .active(true)
                            .build());

            long nextVal = leadFormat.getNextSequence();
            String leadId;
            if (Boolean.TRUE.equals(leadFormat.getIncludeYear())) {
                leadId = leadFormat.getPrefix() + currentYear + String.format("%0" + leadFormat.getPaddingLength() + "d", nextVal);
            } else {
                leadId = leadFormat.getPrefix() + String.format("%0" + leadFormat.getPaddingLength() + "d", nextVal);
            }
            
            leadFormat.setNextSequence(nextVal + 1);
            idFormatSettingRepository.save(leadFormat);

            LeadProfile profile = LeadProfile.builder()
                    .user(user)
                    .tenantId(tenantId)
                    .leadId(leadId)
                    .rollNo(request.getProfileData() != null ? (String) request.getProfileData().get("rollNo") : null)
                    .courseId(request.getProfileData() != null && request.getProfileData().get("courseId") != null
                            ? Long.valueOf(request.getProfileData().get("courseId").toString())
                            : null)
                    .build();
            leadProfileRepository.save(profile);
        }

        // Send email to user
        try {
            String loginId = user.getEmail();
            String loginUrl = frontendUrl + "/login";
            
            // Try to find custom domain
            String customDomain = null;
            Long originalTenant = TenantContext.getCurrentTenant();
            String originalCode = TenantContext.getCurrentTenantCode();
            try {
                TenantContext.clear();
                com.project.www.entity.Tenant t = tenantRepository.findById(tenantId).orElse(null);
                if (t != null && t.getDomain() != null && !t.getDomain().trim().isEmpty()) {
                    customDomain = t.getDomain();
                    loginUrl = "http://" + customDomain + ":5173/login";
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

        List<User> users = userRepository.findAllByTenantId(tenantId);
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

        if (request.getSupervisorUserId() != null) {
            User supervisor = userRepository.findByIdAndTenantId(request.getSupervisorUserId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Supervisor user not found"));
            userReportingRepository.deleteAllByUserIdAndTenantId(updated.getId(), tenantId);
            UserReporting reporting = UserReporting.builder()
                    .tenantId(tenantId)
                    .user(updated)
                    .supervisorUser(supervisor)
                    .build();
            userReportingRepository.save(reporting);
        } else {
            userReportingRepository.deleteAllByUserIdAndTenantId(updated.getId(), tenantId);
        }

        return mapToResponse(updated);
    }

    /**
     * Soft-deactivates a user. Physical deletion is not permitted to preserve audit history
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
                long activeSuperAdmins = userRepository.countByRoleNameAndTenantIdAndActiveTrue("SUPER_ADMIN", tenantId);
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
}
