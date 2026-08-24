package com.project.www.accessmanagement.controller;

import com.project.www.dto.SupervisorResponse;

import com.project.www.accessmanagement.entity.User;

import com.project.www.accessmanagement.dto.CreateUserRequest;
import com.project.www.accessmanagement.dto.UserResponse;
import com.project.www.accessmanagement.dto.ResetPasswordRequest;
import com.project.www.accessmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import com.project.www.accessmanagement.repository.UserRepository;

@RestController
@RequestMapping({"/users", "/employees", "/api/users", "/api/employees", "/api/auth/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final com.project.www.tenant.repository.TenantModuleRepository tenantModuleRepository;
    private final com.project.www.accessmanagement.repository.UserReportingRepository userReportingRepository;

    @PostMapping
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public String createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        userService.createUser(request);
        return "User Created Successfully";
    }

    @GetMapping
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping({"/supervisors", "/supervisors/"})
    @PreAuthorize("isAuthenticated()")
    public List<com.project.www.dto.SupervisorResponse> getSupervisors(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String roleCode
    ) {
        return userService.getSupervisorsForRole(roleId, roleCode);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findFirstByEmailAndTenantId(email, com.project.www.util.TenantContext.getCurrentTenant())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("name", user.getFirstName() + " " + user.getLastName());
        response.put("role", user.getRole() != null ? user.getRole().getName() : null);
        response.put("email", user.getEmail());
        
        java.util.Set<String> allPermissions = new java.util.HashSet<>();
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().forEach(p -> allPermissions.add(p.getPermissionKey()));
        }
        if (user.getPermissions() != null) {
            user.getPermissions().forEach(p -> allPermissions.add(p.getPermissionKey()));
        }
        response.put("permissions", new java.util.ArrayList<>(allPermissions));
        java.util.Set<String> allModules = new java.util.HashSet<>();
        
        List<com.project.www.tenant.entity.TenantModule> activeTenantModules = new java.util.ArrayList<>();
        String ogCode = com.project.www.util.TenantContext.getCurrentTenantCode();
        Long ogId = com.project.www.util.TenantContext.getCurrentTenant();
        try {
            com.project.www.util.TenantContext.clear();
            activeTenantModules = tenantModuleRepository.findByTenantIdAndActiveTrue(user.getTenantId());
        } catch (Exception e) {
            System.err.println("Failed to fetch tenant modules in getMe: " + e.getMessage());
        } finally {
            com.project.www.util.TenantContext.setCurrentTenantCode(ogCode);
            com.project.www.util.TenantContext.setCurrentTenant(ogId);
        }
        
        java.util.Set<String> activeTenantModuleNames = activeTenantModules.stream()
                .map(com.project.www.tenant.entity.TenantModule::getModuleName)
                .collect(java.util.stream.Collectors.toSet());

        if (user.getRole() != null && ("SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()) || "SYSTEM_SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()))) {
            allModules.addAll(activeTenantModuleNames);
        } else {
            if (user.getModules() != null) {
                allModules.addAll(user.getModules());
            }
            if (allModules.isEmpty()) {
                allPermissions.forEach(p -> {
                    if (p.startsWith("SUPPORT_TICKETS")) {
                        allModules.add("SUPPORT_TICKETS");
                    } else {
                        allModules.add(p.split("_")[0]);
                    }
                });
            }
            // Ensure regular users cannot see modules the tenant has lost access to
            allModules.retainAll(activeTenantModuleNames);
        }
        
        response.put("modules", new java.util.ArrayList<>(allModules));
        response.put("tenantId", user.getTenantId());
        response.put("tenantCode", com.project.www.util.TenantContext.getCurrentTenantCode());
        response.put("isPlatformAdmin", user.getTenantId() != null && user.getTenantId() == 1L);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<UserResponse> getUsersByTenant(
            @PathVariable Long tenantId
    ) {
        return userService.getUsersByTenant(tenantId);
    }

    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("isAuthenticated()")
    public List<UserResponse> getUsersByRole(@PathVariable String roleName) {
        return userService.getAllUsers().stream()
                .filter(u -> u.getRoleName() != null && u.getRoleName().equalsIgnoreCase(roleName) || 
                             (u.getRoleNames() != null && u.getRoleNames().stream().anyMatch(r -> r.equalsIgnoreCase(roleName))))
                .collect(java.util.stream.Collectors.toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_UPDATE)")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_DELETE)")
    public String deleteUser(@PathVariable Long id) {
        // Soft-deactivates. Physical deletion is not permitted.
        userService.deleteUser(id);
        return "User deactivated successfully";
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_UPDATE)")
    public String deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return "User deactivated successfully";
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_UPDATE)")
    public String toggleUserActiveStatus(@PathVariable Long id) {
        userService.toggleUserActiveStatus(id);
        return "User active status toggled successfully";
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_UPDATE)")
    public String resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return "Password Reset Successfully";
    }

    @GetMapping("/access-scope")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.project.www.accessmanagement.dto.AccessScopeResponse> getAccessScope() {
        return ResponseEntity.ok(userService.getAccessScope());
    }

    @GetMapping("/{id}/direct-reports")
    @PreAuthorize("isAuthenticated()")
    public List<UserResponse> getDirectReports(@PathVariable Long id) {
        return userService.getDirectReports(id);
    }

    @GetMapping("/hierarchy/subordinates")
    @PreAuthorize("isAuthenticated()")
    public List<Long> getMySubordinateIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findFirstByEmailAndTenantId(authentication.getName(), com.project.www.util.TenantContext.getCurrentTenant())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getSubordinateIds(currentUser.getId());
    }

    @GetMapping("/hierarchy/subordinates/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<Long> getSubordinateIds(@PathVariable Long userId) {
        Long tenantId = com.project.www.util.TenantContext.getCurrentTenant();
        java.util.Set<Long> seenIds = new java.util.HashSet<>();
        java.util.List<Long> result = new java.util.ArrayList<>();
        
        seenIds.add(userId);
        result.add(userId); // Include the user themselves

        java.util.Queue<Long> queue = new java.util.LinkedList<>();
        queue.add(userId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<com.project.www.accessmanagement.entity.UserReporting> reports = userReportingRepository.findAllBySupervisorUserIdAndTenantId(currentId, tenantId);
            for (com.project.www.accessmanagement.entity.UserReporting report : reports) {
                User sub = report.getUser();
                if (sub != null && !seenIds.contains(sub.getId())) {
                    seenIds.add(sub.getId());
                    result.add(sub.getId());
                    queue.add(sub.getId());
                }
            }
        }
        return result;
    }
}
