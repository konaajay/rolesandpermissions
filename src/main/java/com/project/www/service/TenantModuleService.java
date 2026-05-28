package com.project.www.service;

import com.project.www.entity.TenantModule;
import com.project.www.dto.TenantModuleUpdateRequest;
import java.util.List;

public interface TenantModuleService {
    List<TenantModule> getModulesForTenant(Long tenantId);
    void enableModule(Long tenantId, String moduleName, TenantModuleUpdateRequest request);
    void disableModule(Long tenantId, String moduleName);
}
