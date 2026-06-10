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

@RestController
@RequestMapping({"/users", "/employees"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public String createUser(
            @RequestBody CreateUserRequest request
    ) {
        userService.createUser(request);
        return "User Created Successfully";
    }

    @GetMapping
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/supervisors")
    @PreAuthorize("isAuthenticated()")
    public List<com.project.www.dto.SupervisorResponse> getSupervisors(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String roleCode
    ) {
        return userService.getSupervisorsForRole(roleId, roleCode);
    }

    @GetMapping("/{id}")
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
            @RequestBody CreateUserRequest request
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
}