package com.project.www.integrations.event;

import com.project.www.tenant.entity.Subscription;
import com.project.www.tenant.entity.Tenant;
import com.project.www.tenant.repository.SubscriptionRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class SubscriptionPaymentIntegrationAdapter implements PaymentIntegrationAdapter {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectProvider<SubscriptionPaymentIntegrationAdapter> selfProvider;

    @Override
    public void updatePaymentStatus(String orderId, String status, String provider, Long tenantId) {
        log.info("Received payment update for order {} with status {} for tenant {}", orderId, status, tenantId);
        
        SubscriptionPaymentIntegrationAdapter self = selfProvider.getObject();
        
        Tenant tenant = self.resolveTenantCode(tenantId);
        if (tenant == null) {
            log.error("Tenant {} not found for payment {}", tenantId, orderId);
            return;
        }

        String previousTenant = TenantContext.getCurrentTenantCode();
        boolean isSuccess = false;
        try {
            TenantContext.setCurrentTenantCode(tenant.getCode());
            isSuccess = self.updateSubscriptionInsideTenantDB(orderId, status, tenant);
        } finally {
            if (previousTenant != null) {
                TenantContext.setCurrentTenantCode(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
        
        if (isSuccess) {
            self.updateTenantStatus(tenant);
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public Tenant resolveTenantCode(Long tenantId) {
        String previousTenant = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear(); // Force DEFAULT (Master DB)
            return tenantRepository.findById(tenantId).orElse(null);
        } finally {
            if (previousTenant != null) {
                TenantContext.setCurrentTenantCode(previousTenant);
            }
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public boolean updateSubscriptionInsideTenantDB(String orderId, String status, Tenant tenant) {
        Subscription subscription = subscriptionRepository.findByPaymentReference(orderId).orElse(null);
        
        if (subscription == null) {
            log.warn("Subscription for order {} not found in tenant {}", orderId, tenant.getCode());
            return false;
        }

        if ("SUCCESS".equalsIgnoreCase(status)) {
            subscription.setStatus("ACTIVE");
            tenant.setSubscriptionType(subscription.getPlanName());
            tenant.setSubscriptionStartDate(subscription.getStartDate());
            tenant.setSubscriptionEndDate(subscription.getEndDate());
            tenant.setStatus("ACTIVE");
            tenant.setActive(true);
        } else {
            subscription.setStatus("FAILED");
        }

        subscriptionRepository.save(subscription);
        log.info("Successfully updated subscription {} to {}", subscription.getId(), subscription.getStatus());
        return "SUCCESS".equalsIgnoreCase(status);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateTenantStatus(Tenant tenant) {
        String previousTenant = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            tenantRepository.save(tenant);
        } finally {
            if (previousTenant != null) {
                TenantContext.setCurrentTenantCode(previousTenant);
            }
        }
    }
}
