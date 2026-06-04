package com.project.www.integrations.service;

import java.util.Map;

import com.project.www.integrations.dto.*;

public interface CashfreeService {
    void configure(CashfreeConfigureRequest request);
    Map<String, Object> getStatus();
    Map<String, Object> createOrder(CashfreeCreateOrderRequest request);
    Map<String, Object> createPaymentLink(CashfreePaymentLinkRequest request);
    CashfreePaymentStatusResponse getPaymentStatus(String orderId);
    void handleWebhook(String payload);
    void disconnect();
}
