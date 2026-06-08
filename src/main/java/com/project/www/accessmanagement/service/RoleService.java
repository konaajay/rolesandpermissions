package com.project.www.accessmanagement.service;

import com.project.www.accessmanagement.dto.RoleResponse;

import com.project.www.accessmanagement.dto.RoleHierarchyResponse;

import com.project.www.accessmanagement.dto.MapPermissionsRequest;

import com.project.www.accessmanagement.dto.CreateRoleRequest;

public interface RoleService {

    void createRole(CreateRoleRequest request);

    void updateRole(Long roleId, CreateRoleRequest request);

    void enableRole(Long roleId);

    void disableRole(Long roleId);

    java.util.List<com.project.www.accessmanagement.dto.RoleResponse> getAllRoles();

    void mapPermissions(Long roleId, com.project.www.accessmanagement.dto.MapPermissionsRequest request);

    java.util.List<com.project.www.accessmanagement.dto.RoleHierarchyResponse> getHierarchy();

    void setHierarchy(Long roleId, Long reportsToRoleId);

    void deleteHierarchy(Long roleId, Long reportsToRoleId);
}
