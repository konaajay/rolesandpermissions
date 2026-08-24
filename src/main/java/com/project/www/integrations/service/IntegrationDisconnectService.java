package com.project.www.integrations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.repository.TenantIntegrationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IntegrationDisconnectService {

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationCredentialService credentialService;
    private final IntegrationLogService logService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;

    @Transactional("integrationTransactionManager")
    public void disconnect(String code) {
        var ctx = tenantIntegrationResolver.resolveContext(code);
        TenantIntegration ti = ctx.getTenantIntegration();
        ti.setConnected(false);
        ti.setStatus(IntegrationStatus.DISCONNECTED);
        ti.setHealth(IntegrationHealth.UNKNOWN);
        ti.setDisconnectedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);
        credentialService.clearTokens(ti.getId());

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ti.getId(), code.toUpperCase(), "disconnect", "disconnect",
                null, null, "SUCCESS", 200, null, 0);
    }
}
