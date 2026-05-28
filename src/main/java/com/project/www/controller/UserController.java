package com.project.www.controller;

import com.project.www.dto.CreateUserRequest;
import com.project.www.dto.UserResponse;
import com.project.www.dto.ResetPasswordRequest;
import com.project.www.service.UserService;
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

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<UserResponse> getUsersByTenant(
            @PathVariable Long tenantId
    ) {
        return userService.getUsersByTenant(tenantId);
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
        userService.deleteUser(id);
        return "User Deleted Successfully";
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@moduleEvaluator.hasModule(T(com.project.www.constants.Modules).EMPLOYEE) and hasAuthority(T(com.project.www.constants.CorePermissions).USER_UPDATE)")
    public String resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return "Password Reset Successfully";
    }
}