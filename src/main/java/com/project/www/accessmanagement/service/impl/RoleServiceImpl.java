package com.project.www.accessmanagement.service.impl;

import com.project.www.accessmanagement.dto.MapPermissionsRequest;

import com.project.www.accessmanagement.dto.CreateRoleRequest;
import com.project.www.accessmanagement.dto.RoleHierarchyResponse;
import com.project.www.accessmanagement.entity.Permission;
import com.project.www.accessmanagement.entity.Role;
import com.project.www.accessmanagement.entity.RoleHierarchy;
import com.project.www.accessmanagement.repository.PermissionRepository;
import com.project.www.accessmanagement.repository.RoleHierarchyRepository;
import com.project.www.accessmanagement.repository.RoleRepository;
import com.project.www.accessmanagement.service.RoleService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import com.project.www.accessmanagement.dto.RoleResponse;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleHierarchyRepository roleHierarchyRepository;

    @Override
    @Transactional
    public void createRole(CreateRoleRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = request.getName().trim().toUpperCase().replace(" ", "_");
        } else {
            code = code.trim().toUpperCase().replace(" ", "_");
        }

        boolean existsName = roleRepository.existsByNameAndTenantId(request.getName(), tenantId);
        if (existsName) {
            throw new RuntimeException("Role with this name already exists under this tenant");
        }

        boolean existsCode = roleRepository.existsByCodeAndTenantId(code, tenantId);
        if (existsCode) {
            throw new RuntimeException("Role with this code already exists under this tenant");
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            for (Long permId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findByIdAndTenantId(permId, tenantId)
                        .orElseThrow(() -> new RuntimeException("Permission not found or does not belong to this tenant: " + permId));
                permissions.add(permission);
            }
        }

        Role role = Role.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .showInUserForm(request.getShowInUserForm() != null ? request.getShowInUserForm() : true)
                .permissions(permissions)
                .active(true)
                .build();

        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void updateRole(Long roleId, CreateRoleRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));

        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = request.getName().trim().toUpperCase().replace(" ", "_");
        } else {
            code = code.trim().toUpperCase().replace(" ", "_");
        }

        if (!role.getName().equalsIgnoreCase(request.getName())) {
            boolean exists = roleRepository.existsByNameAndTenantId(request.getName(), tenantId);
            if (exists) {
                throw new RuntimeException("Another role with this name already exists under this tenant");
            }
            role.setName(request.getName());
        }

        if (!role.getCode().equalsIgnoreCase(code)) {
            boolean exists = roleRepository.existsByCodeAndTenantId(code, tenantId);
            if (exists) {
                throw new RuntimeException("Another role with this code already exists under this tenant");
            }
            role.setCode(code);
        }

        role.setDescription(request.getDescription());
        if (request.getShowInUserForm() != null) {
            role.setShowInUserForm(request.getShowInUserForm());
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            for (Long permId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findByIdAndTenantId(permId, tenantId)
                        .orElseThrow(() -> new RuntimeException("Permission not found or does not belong to this tenant: " + permId));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);

        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void enableRole(Long roleId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));

        role.setActive(true);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void disableRole(Long roleId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));

        if ("SUPER_ADMIN".equals(role.getName())) {
            throw new RuntimeException("The SUPER_ADMIN role is system protected and cannot be disabled.");
        }

        role.setActive(false);
        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        return roleRepository.findAllByTenantId(tenantId).stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .tenantId(role.getTenantId())
                        .name(role.getName())
                        .code(role.getCode())
                        .description(role.getDescription())
                        .active(role.getActive())
                        .showInUserForm(role.getShowInUserForm())
                        .permissions(role.getPermissions().stream()
                                .map(Permission::getPermissionKey)
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void mapPermissions(Long roleId, com.project.www.accessmanagement.dto.MapPermissionsRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found or does not belong to this tenant"));

        if ("SUPER_ADMIN".equals(role.getName()) && (request.getPermissionIds() == null || request.getPermissionIds().isEmpty())) {
            throw new RuntimeException("Cannot remove all permissions from the SUPER_ADMIN role.");
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            for (Long permId : request.getPermissionIds()) {
                Permission permission = permissionRepository.findByIdAndTenantId(permId, tenantId)
                        .orElseThrow(() -> new RuntimeException("Permission not found or does not belong to this tenant: " + permId));
                permissions.add(permission);
            }
        }

        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleHierarchyResponse> getHierarchy() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context found");

        List<Role> allRoles = roleRepository.findAllByTenantId(tenantId);
        List<Long> roleIds = allRoles.stream().map(Role::getId).collect(Collectors.toList());
        return roleHierarchyRepository.findAllByRoleIdInAndTenantId(roleIds, tenantId).stream()
                .map(rh -> RoleHierarchyResponse.builder()
                        .id(rh.getId())
                        .roleId(rh.getRole().getId())
                        .roleName(rh.getRole().getName())
                        .roleCode(rh.getRole().getCode())
                        .reportsToRoleId(rh.getReportsToRole().getId())
                        .reportsToRoleName(rh.getReportsToRole().getName())
                        .reportsToRoleCode(rh.getReportsToRole().getCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setHierarchy(Long roleId, Long reportsToRoleId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context found");
        if (roleId.equals(reportsToRoleId)) throw new RuntimeException("A role cannot report to itself");

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        Role supervisor = roleRepository.findByIdAndTenantId(reportsToRoleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Supervisor role not found: " + reportsToRoleId));

        boolean exists = roleHierarchyRepository
                .findByRoleIdAndReportsToRoleIdAndTenantId(roleId, reportsToRoleId, tenantId)
                .isPresent();
        if (exists) throw new RuntimeException("This hierarchy link already exists");

        roleHierarchyRepository.save(RoleHierarchy.builder()
                .tenantId(tenantId)
                .role(role)
                .reportsToRole(supervisor)
                .build());
    }

    @Override
    @Transactional
    public void deleteHierarchy(Long roleId, Long reportsToRoleId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context found");

        RoleHierarchy rh = roleHierarchyRepository
                .findByRoleIdAndReportsToRoleIdAndTenantId(roleId, reportsToRoleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Hierarchy link not found"));
        roleHierarchyRepository.delete(rh);
    }
}