package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.ZapierWebhookConfigureRequest;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.ApiKeyGenerator;
import com.project.www.integrations.util.JsonUtil;

import java.util.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ZapierServiceImpl implements ZapierService {

    private static final String ZAPIER_CODE = "ZAPIER";

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final IntegrationCredentialService credentialService;
    private final IntegrationSettingService settingService;
    private final IntegrationLogService logService;
    private final TenantContextService tenantContextService;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public Map<String, String> generateApiKey() {
        String key = ApiKeyGenerator.generateApiKey();
        String secret = ApiKeyGenerator.generateApiSecret();
        var ctx = tenantIntegrationResolver.resolveContext(ZAPIER_CODE);
        credentialService.saveApiKeySecret(ctx.getTenantIntegration().getId(), key, secret);
        settingService.saveSetting(ctx.getTenantIntegration().getId(), "zapier_key_hash", ApiKeyGenerator.hash(key), false);

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ctx.getTenantIntegration().getId(), ZAPIER_CODE, "api_key", "generate",
                null, "{\"masked\":true}", "SUCCESS", 200, null, 0);

        Map<String, String> result = new HashMap<>();
        result.put("apiKey", key);
        result.put("apiSecret", secret);
        return result;
    }

    @Override
    @Transactional
    public Map<String, String> regenerateApiKey() {
        revokeApiKey();
        return generateApiKey();
    }

    @Override
    @Transactional
    public void revokeApiKey() {
        var ctx = tenantIntegrationResolver.resolveContext(ZAPIER_CODE);
        credentialService.saveApiKeySecret(ctx.getTenantIntegration().getId(), "", "");
        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ctx.getTenantIntegration().getId(), ZAPIER_CODE, "api_key", "revoke",
                null, null, "SUCCESS", 200, null, 0);
    }

    @Override
    public List<String> getTriggers() {
        return List.of(
                "LEAD_CREATED",
                "LEAD_UPDATED",
                "PAYMENT_SUCCESS",
                "PAYMENT_FAILED",
                "SUPPORT_TICKET_CREATED",
                "EMPLOYEE_CREATED",
                "ATTENDANCE_MARKED",
                "INVOICE_CREATED"
        );
    }

    @Override
    public Map<String, Object> getSampleLead() {
        return Map.of(
                "id", 1001,
                "name", "Sample Lead",
                "email", "lead@example.com",
                "phone", "+919999999999",
                "source", "Zapier"
        );
    }

    @Override
    @Transactional
    public void configureWebhook(ZapierWebhookConfigureRequest request) {
        // Validate webhook URL
        if (request.getWebhookUrl() == null || request.getWebhookUrl().isBlank()) {
            throw new IntegrationException("Webhook URL cannot be blank");
        }
        if (!request.getWebhookUrl().startsWith("https://hooks.zapier.com/hooks/catch/")) {
            throw new IntegrationException("Invalid Zapier webhook URL");
        }
        var ctx = tenantIntegrationResolver.resolveContext(ZAPIER_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        // Save configuration
        settingService.saveSetting(ti.getId(), "webhook_url", request.getWebhookUrl(), false);
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            settingService.saveSetting(ti.getId(), "events", String.join(",", request.getEvents()), false);
        }
        // Update integration status
        ti.setEnabled(true);
        ti.setConnected(true);
        ti.setStatus(IntegrationStatus.CONNECTED);
        ti.setHealth(IntegrationHealth.HEALTHY);
        ti.setConnectedAt(LocalDateTime.now());
        // Persist changes
        tenantIntegrationRepository.save(ti);
        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ti.getId(), ZAPIER_CODE, "configure_webhook", "configure",
                JsonUtil.toJson(request), null, "SUCCESS", 200, null, 0);
    }

    @Override
    @Transactional
    public boolean sendEvent(String eventName, Object payload) {
        var ctx = tenantIntegrationResolver.resolveContext(ZAPIER_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        // Check enabled and connected
        if (!Boolean.TRUE.equals(ti.getEnabled()) || !Boolean.TRUE.equals(ti.getConnected())) {
            logService.log(
                tenantContextService.getCurrentTenantId(),
                ti.getId(),
                ZAPIER_CODE,
                eventName,
                "zapier_skipped",
                null,
                null,
                "SKIPPED",
                0,
                "Zapier integration is not enabled or not connected",
                0);
            return false;
        }
        // Validate webhook URL
        String webhookUrl = settingService.getSetting(ti.getId(), "webhook_url").orElse(null);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            logService.log(
                tenantContextService.getCurrentTenantId(),
                ti.getId(),
                ZAPIER_CODE,
                eventName,
                "zapier_skipped",
                null,
                null,
                "SKIPPED",
                0,
                "Zapier webhook_url is missing",
                0);
            return false;
        }
        // Validate event allowed
        String events = settingService.getSetting(ti.getId(), "events").orElse("");
        if (!events.isEmpty()) {
            boolean allowed = java.util.Arrays.stream(events.split(","))
                    .anyMatch(e -> e.equalsIgnoreCase(eventName));
            if (!allowed) {
                logService.log(
                    tenantContextService.getCurrentTenantId(),
                    ti.getId(),
                    ZAPIER_CODE,
                    eventName,
                    "zapier_skipped",
                    null,
                    null,
                    "SKIPPED",
                    0,
                    "Event is not allowed for this Zapier configuration: " + eventName,
                    0);
                return false;
            }
        }
        Long tenantId = tenantContextService.getCurrentTenantId();
        // Build flat payload
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("eventType", eventName);
        body.put("tenantId", tenantId);
        body.put("triggeredAt", java.time.LocalDateTime.now().toString());
        if (payload instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> mapPayload = (java.util.Map<String, Object>) payload;
            body.putAll(mapPayload);
        } else {
            body.put("payload", payload);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, requestEntity, String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            logService.log(tenantId, ti.getId(), ZAPIER_CODE, eventName, "zapier_send",
                    JsonUtil.toJson(body), response.getBody(),
                    success ? "SUCCESS" : "FAILED",
                    response.getStatusCode().value(),
                    success ? null : "Zapier returned non-2xx response",
                    0);
            return success;
        } catch (Exception e) {
            logService.log(tenantId, ti.getId(), ZAPIER_CODE, eventName, "zapier_send",
                    JsonUtil.toJson(body), null, "FAILED", 500, e.getMessage(), 0);
            return false;
        }
    }

}
