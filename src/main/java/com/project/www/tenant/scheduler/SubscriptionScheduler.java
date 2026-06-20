package com.project.www.tenant.scheduler;

import com.project.www.tenant.entity.Tenant;
import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final TenantRepository tenantRepository;
    private final TenantModuleRepository tenantModuleRepository;

    /**
     * Runs daily at midnight (00:00) to check for expired trials/subscriptions
     * and disables tenant modules if the end date is crossed.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processExpiredSubscriptions() {
        log.info("Starting daily subscription expiry check...");
        
        // Find tenants whose subscription end date is before today and who are still ACTIVE or TRIAL
        LocalDate today = LocalDate.now();
        List<Tenant> tenantsToCheck = tenantRepository.findAll();
        
        for (Tenant tenant : tenantsToCheck) {
            if (tenant.getSubscriptionEndDate() != null && tenant.getSubscriptionEndDate().isBefore(today)) {
                if ("ACTIVE".equalsIgnoreCase(tenant.getStatus()) || "TRIAL".equalsIgnoreCase(tenant.getStatus())) {
                    log.info("Tenant {} ({}) subscription/trial has expired.", tenant.getName(), tenant.getCode());
                    
                    // Mark tenant as EXPIRED
                    tenant.setStatus("EXPIRED");
                    tenantRepository.save(tenant);
                    
                    // Clear and set context to Master to be safe, though this is master DB
                    String ogCode = TenantContext.getCurrentTenantCode();
                    Long ogId = TenantContext.getCurrentTenant();
                    
                    try {
                        TenantContext.clear();
                        
                        // Disable all modules for this tenant
                        List<TenantModule> activeModules = tenantModuleRepository.findByTenantId(tenant.getId());
                        for (TenantModule module : activeModules) {
                            module.setActive(false);
                            tenantModuleRepository.save(module);
                        }
                        
                    } finally {
                        TenantContext.setCurrentTenant(ogId);
                        TenantContext.setCurrentTenantCode(ogCode);
                    }
                }
            }
        }
        log.info("Finished daily subscription expiry check.");
    }
}
