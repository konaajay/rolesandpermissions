package com.project.www.integrations.service.impl;

import com.project.www.tenant.entity.Tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.config.IntegrationProperties;
import com.project.www.integrations.dto.WebhookDeliveryLogResponse;
import com.project.www.integrations.dto.WebhookSubscriptionRequest;
import com.project.www.integrations.dto.WebhookSubscriptionResponse;
import com.project.www.integrations.entity.IntegrationDefinition;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.entity.WebhookDeliveryLog;
import com.project.www.integrations.entity.WebhookSubscription;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.enums.WebhookDeliveryStatus;
import com.project.www.integrations.exception.IntegrationNotFoundException;
import com.project.www.integrations.repository.IntegrationDefinitionRepository;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.repository.WebhookDeliveryLogRepository;
import com.project.www.integrations.repository.WebhookSubscriptionRepository;
import com.project.www.integrations.service.EncryptionService;
import com.project.www.integrations.service.TenantContextService;
import com.project.www.integrations.service.WebhookService;
import com.project.www.integrations.util.ApiKeyGenerator;
import com.project.www.integrations.util.DateFormatUtil;
import com.project.www.integrations.util.HmacUtil;
import com.project.www.integrations.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final IntegrationDefinitionRepository integrationDefinitionRepository;
    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final TenantContextService tenantContextService;
    private final EncryptionService encryptionService;
    private final IntegrationProperties integrationProperties;
    private final RestTemplate restTemplate;

    @Override
    @Transactional("integrationTransactionManager")
    public WebhookSubscriptionResponse create(WebhookSubscriptionRequest request) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        String secret = ApiKeyGenerator.generateApiSecret();
        WebhookSubscription sub = WebhookSubscription.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .webhookUrl(request.getWebhookUrl())
                .secretKeyEncrypted(encryptionService.encrypt(secret))
                .events(request.getEvents() != null ? String.join(",", request.getEvents()) : "")
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();
        WebhookSubscription saved = subscriptionRepository.save(sub);
        
        // Ensure WEBHOOK integration row exists and mark it connected
        IntegrationDefinition definition = integrationDefinitionRepository.findByCode("WEBHOOK")
                .orElseThrow(() -> new RuntimeException("WEBHOOK integration definition not found"));
        TenantIntegration integration = tenantIntegrationRepository.findByTenantIdAndCode(tenantId, "WEBHOOK")
                .orElseGet(() -> {
                    TenantIntegration newIntegration = new TenantIntegration();
                    newIntegration.setTenantId(tenantId);
                    newIntegration.setCode("WEBHOOK");
                    newIntegration.setIntegrationDefinitionId(definition.getId());
                    newIntegration.setEnvironment("sandbox");
                    return newIntegration;
                });
        
        integration.setConnected(true);
        integration.setEnabled(true);
        integration.setStatus(IntegrationStatus.CONNECTED);
        integration.setHealth(IntegrationHealth.HEALTHY);
        integration.setConnectedAt(LocalDateTime.now());
        integration.setDisconnectedAt(null);
        tenantIntegrationRepository.save(integration);
        
        return toResponse(saved);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public List<WebhookSubscriptionResponse> list() {
        Long tenantId = tenantContextService.getCurrentTenantId();
        return subscriptionRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional("integrationTransactionManager")
    public WebhookSubscriptionResponse update(Long id, WebhookSubscriptionRequest request) {
        WebhookSubscription sub = findSubscription(id);
        sub.setName(request.getName());
        sub.setWebhookUrl(request.getWebhookUrl());
        if (request.getEvents() != null) {
            sub.setEvents(String.join(",", request.getEvents()));
        }
        if (request.getEnabled() != null) {
            sub.setEnabled(request.getEnabled());
        }
        return toResponse(subscriptionRepository.save(sub));
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void delete(Long id) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        WebhookSubscription sub = findSubscription(id);
        subscriptionRepository.delete(sub);
        // If no more subscriptions, mark integration as disconnected
        List<WebhookSubscription> remaining = subscriptionRepository.findByTenantId(tenantId);
        if (remaining.isEmpty()) {
            tenantIntegrationRepository.findByTenantIdAndCode(tenantId, "WEBHOOK")
                    .ifPresent(integration -> {
                        integration.setConnected(false);
                        integration.setStatus(IntegrationStatus.DISCONNECTED);
                        integration.setHealth(IntegrationHealth.UNKNOWN);
                        integration.setDisconnectedAt(LocalDateTime.now());
                        tenantIntegrationRepository.save(integration);
                    });
        }
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void test(Long id) {
        deliverToSubscription(findSubscription(id), "test.event", Map.of("message", "Test webhook delivery"), "TEST", null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public Page<WebhookDeliveryLogResponse> getLogs(Long id, Pageable pageable) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        return deliveryLogRepository.findByTenantIdAndWebhookSubscriptionIdOrderByCreatedAtDesc(tenantId, id, pageable)
                .map(log -> WebhookDeliveryLogResponse.builder()
                        .id(log.getId())
                        .eventName(log.getEventName())
                        .status(log.getStatus() != null ? log.getStatus().name() : null)
                        .httpStatus(log.getHttpStatus())
                        .retryCount(log.getRetryCount())
                        .date(DateFormatUtil.formatDisplay(log.getCreatedAt()))
                        .build());
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void retry(Long subscriptionId, Long logId) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        WebhookDeliveryLog log = deliveryLogRepository.findByTenantIdAndId(tenantId, logId)
                .orElseThrow(() -> new IntegrationNotFoundException("Delivery log not found"));
        WebhookSubscription sub = findSubscription(subscriptionId);
        try {
            Map<String, Object> payload = JsonUtil.fromJson(log.getPayload(), Map.class);
            deliverToSubscription(sub, log.getEventName(), payload, "RETRY", null);
        } catch (Exception e) {
            log.setStatus(WebhookDeliveryStatus.FAILED);
            log.setRetryCount(log.getRetryCount() + 1);
            deliveryLogRepository.save(log);
        }
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void deliverEvent(String eventName, Map<String, Object> payload, String module, Long referenceId) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        List<WebhookSubscription> subs = subscriptionRepository.findByTenantIdAndEnabledTrue(tenantId);
        Map<String, Object> body = buildPayload(eventName, tenantId, module, referenceId, payload);
        for (WebhookSubscription sub : subs) {
            boolean shouldDeliver = sub.getEvents() == null
                    || sub.getEvents().isBlank()
                    || Arrays.stream(sub.getEvents().split(","))
                        .map(String::trim)
                        .anyMatch(eventName::equals);
                if (shouldDeliver) {
                    deliverToSubscription(sub, eventName, body, module, referenceId);
                }
        }
    }

    private void deliverToSubscription(WebhookSubscription sub, String eventName, Map<String, Object> body,
                                     String module, Long referenceId) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        String payloadJson = JsonUtil.toJson(body);
        String secret = encryptionService.decrypt(sub.getSecretKeyEncrypted());
        String signature = HmacUtil.sign(payloadJson, secret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Webhook-Signature", signature);
        headers.set("X-Webhook-Event", eventName);
        headers.set("X-Tenant-Id", String.valueOf(tenantId));

        WebhookDeliveryLog log = WebhookDeliveryLog.builder()
                .tenantId(tenantId)
                .webhookSubscriptionId(sub.getId())
                .eventName(eventName)
                .payload(payloadJson)
                .retryCount(0)
                .status(WebhookDeliveryStatus.PENDING)
                .build();

        try {
            ResponseEntity<String> response = restTemplate.exchange(sub.getWebhookUrl(), HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);
            log.setResponse(response.getBody());
            log.setHttpStatus(response.getStatusCode().value());
            log.setStatus(response.getStatusCode().is2xxSuccessful()
                    ? WebhookDeliveryStatus.SUCCESS : WebhookDeliveryStatus.FAILED);
        } catch (Exception e) {
            log.setStatus(WebhookDeliveryStatus.FAILED);
            log.setResponse(e.getMessage());
            log.setNextRetryAt(LocalDateTime.now().plusMinutes(integrationProperties.getWebhook().getRetryDelayMinutes()));
        }
        deliveryLogRepository.save(log);
    }

    private Map<String, Object> buildPayload(String eventName, Long tenantId, String module,
                                             Long referenceId, Map<String, Object> data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", eventName);
        body.put("tenantId", tenantId);
        body.put("module", module);
        body.put("referenceId", referenceId);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("data", data != null ? data : Map.of());
        return body;
    }

    private WebhookSubscription findSubscription(Long id) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        return subscriptionRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IntegrationNotFoundException("Webhook subscription not found"));
    }

    private WebhookSubscriptionResponse toResponse(WebhookSubscription sub) {
        List<String> events = sub.getEvents() != null && !sub.getEvents().isBlank()
                ? Arrays.asList(sub.getEvents().split(",")) : List.of();
        return WebhookSubscriptionResponse.builder()
                .id(sub.getId())
                .name(sub.getName())
                .webhookUrl(sub.getWebhookUrl())
                .events(events)
                .enabled(Boolean.TRUE.equals(sub.getEnabled()))
                .build();
    }
}
