package com.project.www.tenant.service;

import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.dto.TenantModuleUpdateRequest;
import java.util.List;

public interface TenantModuleService {
    List<TenantModule> getModulesForTenant(Long tenantId);
    void enableModule(Long tenantId, String moduleName, TenantModuleUpdateRequest request);
    void disableModule(Long tenantId, String moduleName);
    void saveBulkModules(Long tenantId, com.project.www.tenant.dto.BulkModuleSaveRequest request);
    List<com.project.www.tenant.entity.TenantInvoice> getInvoicesForTenant(Long tenantId);
    List<com.project.www.tenant.entity.TenantInvoiceInstallment> getInstallmentsForInvoice(Long invoiceId);
    void payInstallment(Long invoiceId, Long installmentId);
}
