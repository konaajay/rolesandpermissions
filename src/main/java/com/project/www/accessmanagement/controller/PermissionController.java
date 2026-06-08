package com.project.www.accessmanagement.controller;

import com.project.www.accessmanagement.entity.Permission;

import com.project.www.accessmanagement.dto.CreatePermissionRequest;
import com.project.www.accessmanagement.dto.PermissionResponse;
import com.project.www.accessmanagement.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).PERMISSION_CREATE)")
    public String createPermission(
            @RequestBody CreatePermissionRequest request
    ) {
        permissionService.createPermission(request);
        return "Permission Created Successfully";
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).PERMISSION_ENABLE)")
    public String enablePermission(
            @PathVariable Long id
    ) {
        permissionService.enablePermission(id);
        return "Permission Enabled Successfully";
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).PERMISSION_DISABLE)")
    public String disablePermission(
            @PathVariable Long id
    ) {
        permissionService.disablePermission(id);
        return "Permission Disabled Successfully";
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<PermissionResponse> getAllPermissions() {
        return permissionService.getAllPermissions();
    }
}
