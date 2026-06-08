package com.project.www.accessmanagement.service;

import com.project.www.accessmanagement.dto.PermissionResponse;

import com.project.www.accessmanagement.dto.CreatePermissionRequest;

public interface PermissionService {
    void createPermission(CreatePermissionRequest request);
    void enablePermission(Long permissionId);
    void disablePermission(Long permissionId);
    java.util.List<com.project.www.accessmanagement.dto.PermissionResponse> getAllPermissions();
}
