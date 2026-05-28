package com.project.www.service;

import com.project.www.dto.CreatePermissionRequest;

public interface PermissionService {
    void createPermission(CreatePermissionRequest request);
    void enablePermission(Long permissionId);
    void disablePermission(Long permissionId);
    java.util.List<com.project.www.dto.PermissionResponse> getAllPermissions();
}
