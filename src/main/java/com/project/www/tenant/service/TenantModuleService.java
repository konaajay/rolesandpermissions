package com.project.www.tenant.service;

import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.dto.TenantModuleUpdateRequest;
import java.util.List;

public interface TenantModuleService {
    List<TenantModule> getModulesForTenant(Long tenantId);
    void enableModule(Long tenantId, String moduleName, TenantModuleUpdateRequest request);
    void disableModule(Long tenantId, String moduleName);
}
