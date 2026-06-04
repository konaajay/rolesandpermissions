package com.project.www.integrations.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpPaymentIntegrationAdapter implements PaymentIntegrationAdapter {

    @Override
    public void updatePaymentStatus(String orderId, String status, String provider, Long tenantId) {
        log.info("PaymentIntegrationAdapter: order {} status {} from {} for tenant {} - logged until PaymentService is wired",
                orderId, status, provider, tenantId);
    }
}
