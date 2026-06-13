package com.project.www.accessmanagement.service.impl;

import com.project.www.accessmanagement.dto.CreatePermissionRequest;
import com.project.www.accessmanagement.dto.PermissionResponse;
import com.project.www.accessmanagement.entity.Permission;
import com.project.www.accessmanagement.repository.PermissionRepository;
import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.tenant.entity.TenantModule;
import com.project.www.accessmanagement.service.PermissionService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final TenantModuleRepository tenantModuleRepository;

    @Override
    @Transactional
    public void createPermission(CreatePermissionRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        String permissionKey = (request.getModule().trim() + "_" + request.getAction().trim()).toUpperCase();
        boolean exists = permissionRepository.existsByPermissionKeyAndTenantId(permissionKey, tenantId);
        if (exists) {
            throw new RuntimeException("Permission '" + permissionKey + "' already exists under this tenant");
        }

        Permission permission = Permission.builder()
                .tenantId(tenantId)
                .module(request.getModule())
                .action(request.getAction())
                .description(request.getDescription())
                .active(true)
                .build();

        permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void enablePermission(Long permissionId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Permission permission = permissionRepository.findByIdAndTenantId(permissionId, tenantId)
                .orElseThrow(() -> new RuntimeException("Permission not found or does not belong to this tenant"));

        permission.setActive(true);
        permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void disablePermission(Long permissionId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Permission permission = permissionRepository.findByIdAndTenantId(permissionId, tenantId)
                .orElseThrow(() -> new RuntimeException("Permission not found or does not belong to this tenant"));

        permission.setActive(false);
        permissionRepository.save(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        Long tenantId = TenantContext.getCurrentTenant();
        String tenantCode = TenantContext.getCurrentTenantCode();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        // Fetch active modules from Master DB
        java.util.Set<String> activeModules;
        try {
            TenantContext.clear();
            activeModules = tenantModuleRepository.findByTenantIdAndActiveTrue(tenantId)
                    .stream()
                    .map(TenantModule::getModuleName)
                    .collect(Collectors.toSet());
        } finally {
            TenantContext.setCurrentTenant(tenantId);
            TenantContext.setCurrentTenantCode(tenantCode);
        }

        List<String> coreModules = java.util.Arrays.asList(
                "USER", "ROLE", "TENANT", "PERMISSION", "COMPANY_PROFILE", 
                "SETTINGS_MANAGE", "SUBSCRIPTION", "SETTINGS", "DASHBOARD"
        );
        return permissionRepository.findAllByTenantId(tenantId).stream()
                .filter(p -> coreModules.contains(p.getModule()) || activeModules.contains(p.getModule()))
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .tenantId(p.getTenantId())
                        .module(p.getModule())
                        .action(p.getAction())
                        .permissionKey(p.getPermissionKey())
                        .description(p.getDescription())
                        .active(p.getActive())
                        .build())
                .collect(Collectors.toList());
    }
}
