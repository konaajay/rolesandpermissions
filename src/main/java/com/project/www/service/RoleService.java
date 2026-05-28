package com.project.www.service;

import com.project.www.dto.CreateRoleRequest;

public interface RoleService {

    void createRole(CreateRoleRequest request);

    void updateRole(Long roleId, CreateRoleRequest request);

    void enableRole(Long roleId);

    void disableRole(Long roleId);

    java.util.List<com.project.www.dto.RoleResponse> getAllRoles();

    void mapPermissions(Long roleId, com.project.www.dto.MapPermissionsRequest request);

    java.util.List<com.project.www.dto.RoleHierarchyResponse> getHierarchy();

    void setHierarchy(Long roleId, Long reportsToRoleId);

    void deleteHierarchy(Long roleId, Long reportsToRoleId);
}