package com.project.www.accessmanagement.controller;

import com.project.www.accessmanagement.entity.Role;

import com.project.www.accessmanagement.dto.MapPermissionsRequest;

import com.project.www.accessmanagement.dto.CreateRoleRequest;
import com.project.www.accessmanagement.dto.RoleResponse;
import com.project.www.accessmanagement.dto.RoleHierarchyResponse;
import com.project.www.accessmanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_CREATE)")
    public String createRole(
            @RequestBody CreateRoleRequest request
    ) {
        roleService.createRole(request);
        return "Role Created Successfully";
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_UPDATE)")
    public String updateRole(
            @PathVariable Long id,
            @RequestBody CreateRoleRequest request
    ) {
        roleService.updateRole(id, request);
        return "Role Updated Successfully";
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_ENABLE)")
    public String enableRole(
            @PathVariable Long id
    ) {
        roleService.enableRole(id);
        return "Role Enabled Successfully";
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_DISABLE)")
    public String disableRole(
            @PathVariable Long id
    ) {
        roleService.disableRole(id);
        return "Role Disabled Successfully";
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_CREATE)")
    public String mapPermissions(
            @PathVariable Long roleId,
            @RequestBody com.project.www.accessmanagement.dto.MapPermissionsRequest request
    ) {
        roleService.mapPermissions(roleId, request);
        return "Permissions mapped successfully";
    }

    // ── Role Hierarchy Management ──────────────────────────────────────────────

    @GetMapping("/hierarchy")
    @PreAuthorize("isAuthenticated()")
    public List<RoleHierarchyResponse> getHierarchy() {
        return roleService.getHierarchy();
    }

    @PostMapping("/hierarchy")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_UPDATE)")
    public String setHierarchy(
            @RequestParam Long roleId,
            @RequestParam Long reportsToRoleId
    ) {
        roleService.setHierarchy(roleId, reportsToRoleId);
        return "Hierarchy link created";
    }

    @DeleteMapping("/hierarchy")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_UPDATE)")
    public String deleteHierarchy(
            @RequestParam Long roleId,
            @RequestParam Long reportsToRoleId
    ) {
        roleService.deleteHierarchy(roleId, reportsToRoleId);
        return "Hierarchy link removed";
    }
}