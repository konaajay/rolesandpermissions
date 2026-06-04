package com.project.www.integrations.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Common event publisher for other platform modules.
 * Call publish() when CRM, HRMS, LMS, Payment, etc. events occur.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationEventPublisher {

    private final TenantContextService tenantContextService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final WebhookService webhookService;
    private final ZapierService zapierService;
    private final IntegrationLogService logService;

    public void publish(String eventName, Object payload, String module, Long referenceId) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        Map<String, Object> data = payload instanceof Map
                ? (Map<String, Object>) payload
                : Map.of("payload", payload);

        try {
            webhookService.deliverEvent(eventName, data, module, referenceId);
        } catch (Exception e) {
            log.warn("Webhook delivery failed for event {}: {}", eventName, e.getMessage());
        }

        try {
            zapierService.sendEvent(eventName, buildZapierPayload(eventName, tenantId, module, referenceId, data));
        } catch (Exception e) {
            log.warn("Zapier delivery failed for event {}: {}", eventName, e.getMessage());
        }

        tenantIntegrationRepository.findByTenantIdAndCode(tenantId, "WHATSAPP")
                .filter(ti -> Boolean.TRUE.equals(ti.getEnabled()) && Boolean.TRUE.equals(ti.getConnected()))
                .ifPresent(ti -> log.info("WhatsApp notification hook available for event {}", eventName));

        tenantIntegrationRepository.findByTenantIdAndCode(tenantId, "GOOGLE")
                .filter(ti -> Boolean.TRUE.equals(ti.getEnabled()) && Boolean.TRUE.equals(ti.getConnected()))
                .ifPresent(ti -> log.info("Gmail notification hook available for event {}", eventName));

        logService.log(tenantId, null, "SYSTEM", eventName, "event_publish",
                JsonUtil.toJson(Map.of("module", module, "referenceId", referenceId)),
                "{\"dispatched\":true}", "SUCCESS", 200, null, 0);
    }

    private Map<String, Object> buildZapierPayload(String eventName, Long tenantId, String module,
                                                   Long referenceId, Map<String, Object> data) {
        Map<String, Object> body = new HashMap<>();
        body.put("event", eventName);
        body.put("tenantId", tenantId);
        body.put("module", module);
        body.put("referenceId", referenceId);
        body.put("data", data);
        return body;
    }
}
