package com.project.www.security;

import com.project.www.tenant.repository.TenantModuleRepository;


import com.project.www.tenant.entity.TenantModule;

import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("moduleEvaluator")
@RequiredArgsConstructor
public class ModuleEvaluator {

    private final TenantModuleRepository tenantModuleRepository;

    public boolean hasModule(String moduleName) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return false;
        }
        
        // Save original context just in case we need to read from master DB
        // But TenantModule is in master DB and tenantRepository is connected to master DB?
        // Wait, if tenantModule is an Entity in the main package, it connects to the active DataSource!
        // To query TenantModule, we MUST clear the TenantContext, or it will query the tenant's DB!
        
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return tenantModuleRepository.existsByTenantIdAndModuleNameAndActiveTrue(tenantId, moduleName);
        } finally {
            TenantContext.setCurrentTenant(tenantId);
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }
}
