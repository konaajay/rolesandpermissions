package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.event.PaymentIntegrationAdapter;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CashfreeServiceImpl implements CashfreeService {

    private static final String CASHFREE_CODE = "CASHFREE";

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationDisconnectService integrationDisconnectService;
    private final IntegrationCredentialService credentialService;
    private final IntegrationSettingService settingService;
    private final IntegrationLogService logService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;
    private final PaymentIntegrationAdapter paymentAdapter;
    private final RestTemplate restTemplate;

    @Value("${cashfree.sandbox.base-url:https://sandbox.cashfree.com/pg}")
    private String sandboxBaseUrl;

    @Value("${cashfree.production.base-url:https://api.cashfree.com/pg}")
    private String productionBaseUrl;

    @Override
    @Transactional
    public void configure(CashfreeConfigureRequest request) {
        var ctx = tenantIntegrationResolver.resolveContext(CASHFREE_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        credentialService.saveClientCredentials(ti.getId(), request.getAppId(), request.getSecretKey());
        if (request.getEnvironment() != null) {
            ti.setEnvironment(request.getEnvironment().toLowerCase());
            settingService.saveSetting(ti.getId(), "environment", request.getEnvironment().toLowerCase(), false);
        }
        if (request.getReturnUrl() != null) {
            settingService.saveSetting(ti.getId(), "return_url", request.getReturnUrl(), false);
        }
        if (request.getNotifyUrl() != null) {
            settingService.saveSetting(ti.getId(), "notify_url", request.getNotifyUrl(), false);
        }
        ti.setConnected(true);
        ti.setEnabled(true);
        ti.setStatus(IntegrationStatus.CONNECTED);
        ti.setHealth(IntegrationHealth.HEALTHY);
        ti.setConnectedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ti.getId(), CASHFREE_CODE, "configure", "configure", null, null, "SUCCESS", 200, null, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatus() {
        var ctx = tenantIntegrationResolver.resolveContext(CASHFREE_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        return Map.of("connected", ti.getConnected(), "environment", ti.getEnvironment());
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(CashfreeCreateOrderRequest request) {
        var ctx = tenantIntegrationResolver.resolveContext(CASHFREE_CODE);
        Long tiId = ctx.getTenantIntegration().getId();
        String appId = requireClientId(tiId);
        String secret = requireClientSecret(tiId);
        String baseUrl = getBaseUrl(ctx.getTenantIntegration());

        Map<String, Object> orderBody = new LinkedHashMap<>();
        orderBody.put("order_id", request.getOrderId());
        orderBody.put("order_amount", request.getAmount());
        orderBody.put("order_currency", request.getCurrency());
        orderBody.put("customer_details", Map.of(
                "customer_id", "cust_" + (request.getReferenceId() != null ? request.getReferenceId() : request.getOrderId()),
                "customer_name", request.getCustomerName() != null ? request.getCustomerName() : "Customer",
                "customer_email", request.getCustomerEmail() != null ? request.getCustomerEmail() : "customer@example.com",
                "customer_phone", request.getCustomerPhone() != null ? request.getCustomerPhone() : "9999999999"
        ));
        // Add order_meta if return_url / notify_url are configured
        String returnUrl = settingService.getSettingValue(tiId, "return_url");
        String notifyUrl = settingService.getSettingValue(tiId, "notify_url");
        Map<String, Object> orderMeta = new LinkedHashMap<>();
        if (returnUrl != null && !returnUrl.isBlank()) {
            orderMeta.put("return_url", returnUrl.trim() + "?order_id={order_id}");
        }
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            orderMeta.put("notify_url", notifyUrl.trim());
        }
        if (!orderMeta.isEmpty()) {
            orderBody.put("order_meta", orderMeta);
        }

        HttpHeaders headers = cashfreeHeaders(appId, secret);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/orders", HttpMethod.POST,
                new HttpEntity<>(orderBody, headers), Map.class);

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, tiId, CASHFREE_CODE, "create_order", "create",
                JsonUtil.toJson(request), JsonUtil.toJson(response.getBody()),
                response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED",
                response.getStatusCode().value(), null, 0);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IntegrationException("Cashfree order creation failed. Check app ID, secret, and environment.");
        }
        return response.getBody();
    }

    @Override
    @Transactional
    public Map<String, Object> createPaymentLink(CashfreePaymentLinkRequest request) {
        var ctx = tenantIntegrationResolver.resolveContext(CASHFREE_CODE);
        Long tiId = ctx.getTenantIntegration().getId();
        String appId = requireClientId(tiId);
        String secret = requireClientSecret(tiId);
        String baseUrl = getBaseUrl(ctx.getTenantIntegration());

        Map<String, Object> linkBody = Map.of(
                "link_id", request.getLinkId(),
                "link_amount", request.getAmount(),
                "link_currency", request.getCurrency(),
                "link_purpose", "Payment"
        );

        HttpHeaders headers = cashfreeHeaders(appId, secret);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/links", HttpMethod.POST,
                new HttpEntity<>(linkBody, headers), Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IntegrationException("Cashfree payment link creation failed");
        }
        return response.getBody();
    }

    @Override
    @Transactional(readOnly = true)
    public CashfreePaymentStatusResponse getPaymentStatus(String orderId) {
        var ctx = tenantIntegrationResolver.resolveContext(CASHFREE_CODE);
        Long tiId = ctx.getTenantIntegration().getId();
        String appId = requireClientId(tiId);
        String secret = requireClientSecret(tiId);
        String baseUrl = getBaseUrl(ctx.getTenantIntegration());

        HttpHeaders headers = cashfreeHeaders(appId, secret);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IntegrationException("Failed to fetch payment status for order: " + orderId);
        }

        Map<String, Object> body = response.getBody();
        return CashfreePaymentStatusResponse.builder()
                .orderId(orderId)
                .status(String.valueOf(body.getOrDefault("order_status", "UNKNOWN")))
                .paymentStatus(String.valueOf(body.getOrDefault("order_status", "UNKNOWN")))
                .message("Payment status retrieved")
                .build();
    }

    @Override
    @Transactional
    public void handleWebhook(String payload) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        var ctx = tenantIntegrationResolver.resolveContext(CASHFREE_CODE);
        Long tenantIntegrationId = ctx.getTenantIntegration().getId();
        try {
            Map<String, Object> root = JsonUtil.mapper().readValue(payload, Map.class);

            String eventType = String.valueOf(root.getOrDefault("type", "UNKNOWN"));

            String orderId = null;
            String status = null;

            Object dataObj = root.get("data");
            if (dataObj instanceof Map<?, ?> data) {
                Object orderObj = data.get("order");
                Object paymentObj = data.get("payment");
                if (orderObj instanceof Map<?, ?> order) {
                    Object orderIdObj = order.get("order_id");
                    Object orderStatusObj = order.get("order_status");
                    if (orderIdObj != null) {
                        orderId = String.valueOf(orderIdObj);
                    }
                    if (orderStatusObj != null) {
                        status = String.valueOf(orderStatusObj);
                    }
                }
                if (paymentObj instanceof Map<?, ?> payment) {
                    Object paymentStatusObj = payment.get("payment_status");
                    if (paymentStatusObj != null) {
                        status = String.valueOf(paymentStatusObj);
                    }
                }
            }
            // fallback for flat payloads
            if (orderId == null) {
                Object flatOrderId = root.getOrDefault("order_id", root.get("orderId"));
                if (flatOrderId != null) {
                    orderId = String.valueOf(flatOrderId);
                }
            }
            if (status == null) {
                Object flatStatus = root.getOrDefault("order_status", root.get("payment_status"));
                if (flatStatus != null) {
                    status = String.valueOf(flatStatus);
                }
            }
            if (orderId == null || orderId.isBlank() || "null".equalsIgnoreCase(orderId)) {
                throw new IntegrationException("Cashfree webhook missing order_id");
            }
            if (status == null || status.isBlank() || "null".equalsIgnoreCase(status)) {
                throw new IntegrationException("Cashfree webhook missing payment/order status");
            }
            paymentAdapter.updatePaymentStatus(orderId, status, "CASHFREE", tenantId);
            logService.log(
                    tenantId,
                    tenantIntegrationId,
                    CASHFREE_CODE,
                    "payment_webhook",
                    eventType,
                    payload,
                    "orderId=" + orderId + ", status=" + status,
                    "SUCCESS",
                    200,
                    null,
                    0
            );
        } catch (Exception e) {
            logService.log(
                    tenantId,
                    tenantIntegrationId,
                    CASHFREE_CODE,
                    "payment_webhook",
                    "webhook_failed",
                    payload,
                    null,
                    "FAILED",
                    500,
                    e.getMessage(),
                    0
            );
            throw new IntegrationException("Cashfree webhook processing failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void disconnect() {
        integrationDisconnectService.disconnect(CASHFREE_CODE);
    }

    private HttpHeaders cashfreeHeaders(String appId, String secret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", appId);
        headers.set("x-client-secret", secret);
        headers.set("x-api-version", "2023-08-01");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getBaseUrl(TenantIntegration ti) {
        String env = ti.getEnvironment();
        return "production".equalsIgnoreCase(env) ? productionBaseUrl : sandboxBaseUrl;
    }

    private String requireClientId(Long tenantIntegrationId) {
        String appId = credentialService.getDecryptedClientId(tenantIntegrationId);
        if (appId == null || appId.isBlank()) {
            throw new CredentialMissingException("Cashfree app ID not configured");
        }
        return appId;
    }

    private String requireClientSecret(Long tenantIntegrationId) {
        String secret = credentialService.getDecryptedClientSecret(tenantIntegrationId);
        if (secret == null || secret.isBlank()) {
            throw new CredentialMissingException("Cashfree secret key not configured");
        }
        return secret;
    }
}
