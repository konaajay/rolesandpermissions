package com.project.www.service.impl;

import com.project.www.entity.TenantModule;
import com.project.www.repository.TenantModuleRepository;
import com.project.www.service.TenantModuleService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantModuleServiceImpl implements TenantModuleService {

    private final TenantModuleRepository tenantModuleRepository;

    @Override
    public List<TenantModule> getModulesForTenant(Long tenantId) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return tenantModuleRepository.findByTenantId(tenantId);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    public void enableModule(Long tenantId, String moduleName, com.project.www.dto.TenantModuleUpdateRequest request) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            TenantModule module = tenantModuleRepository.findByTenantIdAndModuleName(tenantId, moduleName)
                    .orElseGet(() -> TenantModule.builder()
                            .tenantId(tenantId)
                            .moduleName(moduleName)
                            .build());
            module.setActive(true);
            if (request != null) {
                module.setAmount(request.getAmount());
                module.setPaymentMethod(request.getPaymentMethod());
                module.setSpecialRequirements(request.getSpecialRequirements());
                module.setExtraCharges(request.getExtraCharges());
            }
            tenantModuleRepository.save(module);
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @Override
    public void disableModule(Long tenantId, String moduleName) {
        String originalCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            tenantModuleRepository.findByTenantIdAndModuleName(tenantId, moduleName).ifPresent(module -> {
                module.setActive(false);
                tenantModuleRepository.save(module);
            });
        } finally {
            TenantContext.setCurrentTenantCode(originalCode);
        }
    }
}
