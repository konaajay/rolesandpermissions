package com.project.www.integrations.event;

/**
 * Adapter hook for Payment module.
 * TODO: Wire to existing PaymentService when payment module is available.
 */
public interface PaymentIntegrationAdapter {

    void updatePaymentStatus(String orderId, String status, String provider, Long tenantId);
}
