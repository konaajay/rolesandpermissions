package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.MetaConfigureRequest;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.event.LeadIntegrationAdapter;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MetaServiceImpl implements MetaService {

    private static final String META_CODE = "META";
    private static final String GRAPH_BASE = "https://graph.facebook.com/v19.0";

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationCredentialService credentialService;
    private final IntegrationSettingService settingService;
    private final IntegrationLogService logService;
    private final IntegrationSyncHistoryService syncHistoryService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;
    private final LeadIntegrationAdapter leadAdapter;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public void configure(MetaConfigureRequest request) {
        var ctx = tenantIntegrationResolver.resolveContext(META_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        credentialService.saveAccessToken(ti.getId(), request.getAccessToken(), null, null, "meta");
        if (request.getPageId() != null) {
            settingService.saveSetting(ti.getId(), "page_id", request.getPageId(), false);
        }
        if (request.getFormId() != null) {
            settingService.saveSetting(ti.getId(), "form_id", request.getFormId(), false);
        }
        if (request.getWebhookVerifyToken() != null) {
            settingService.saveSetting(ti.getId(), "verify_token", request.getWebhookVerifyToken(), true);
        }
        if (request.getEnvironment() != null) {
            ti.setEnvironment(request.getEnvironment());
        }
        ti.setConnected(true);
        ti.setEnabled(true);
        ti.setStatus(IntegrationStatus.CONNECTED);
        ti.setHealth(IntegrationHealth.HEALTHY);
        ti.setConnectedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ti.getId(), META_CODE, "configure", "configure", null, null, "SUCCESS", 200, null, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatus() {
        var ctx = tenantIntegrationResolver.resolveContext(META_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        Map<String, Object> status = new HashMap<>();
        status.put("connected", ti.getConnected());
        status.put("enabled", ti.getEnabled());
        status.put("pageId", settingService.getSetting(ti.getId(), "page_id").orElse(null));
        status.put("formId", settingService.getSetting(ti.getId(), "form_id").orElse(null));
        return status;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPages() {
        String token = getAccessToken();
        String url = GRAPH_BASE + "/me/accounts?access_token=" + token;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        if (response.getBody() == null || !response.getBody().containsKey("data")) {
            return List.of();
        }
        return (List<Map<String, Object>>) response.getBody().get("data");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getForms() {
        var ctx = tenantIntegrationResolver.resolveContext(META_CODE);
        String pageId = settingService.getSetting(ctx.getTenantIntegration().getId(), "page_id")
                .orElseThrow(() -> new CredentialMissingException("Meta page_id not configured"));
        String token = getAccessToken();
        String url = GRAPH_BASE + "/" + pageId + "/leadgen_forms?access_token=" + token;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        if (response.getBody() == null || !response.getBody().containsKey("data")) {
            return List.of();
        }
        return (List<Map<String, Object>>) response.getBody().get("data");
    }

    @Override
    @Transactional
    public Map<String, Object> syncLeads() {
        var ctx = tenantIntegrationResolver.resolveContext(META_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        String formId = settingService.getSetting(ti.getId(), "form_id")
                .orElseThrow(() -> new CredentialMissingException("Meta form_id not configured"));
        String token = getAccessToken();
        String url = GRAPH_BASE + "/" + formId + "/leads?access_token=" + token;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        int processed = 0, success = 0, failed = 0;
        if (response.getBody() != null && response.getBody().get("data") instanceof List<?> data) {
            for (Object item : data) {
                processed++;
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> lead = (Map<String, Object>) item;
                    leadAdapter.createLeadFromExternalPayload("META", lead);
                    success++;
                } catch (Exception e) {
                    failed++;
                }
            }
        }

        Long tenantId = tenantContextService.getCurrentTenantId();
        syncHistoryService.record(tenantId, ti.getId(), "meta_leads", "Success",
                "Synced " + success + " leads", processed, success, failed);
        logService.log(tenantId, ti.getId(), META_CODE, "sync_leads", "sync",
                null, JsonUtil.toJson(response.getBody()), "SUCCESS", 200, null, 0);

        ti.setLastSyncedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);
        return Map.of("processed", processed, "success", success, "failed", failed);
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        var ctx = tenantIntegrationResolver.resolveContext(META_CODE);
        logService.log(tenantId, ctx.getTenantIntegration().getId(), META_CODE, "webhook", "receive",
                payload, null, "SUCCESS", 200, null, 0);
        try {
            Map<String, Object> data = JsonUtil.mapper().readValue(payload, Map.class);
            leadAdapter.createLeadFromExternalPayload("META_WEBHOOK", data);
        } catch (Exception e) {
            logService.log(tenantId, ctx.getTenantIntegration().getId(), META_CODE, "webhook", "receive",
                    payload, null, "FAILED", 500, e.getMessage(), 0);
        }
    }

    private String getAccessToken() {
        var ctx = tenantIntegrationResolver.resolveContext(META_CODE);
        String token = credentialService.getDecryptedAccessToken(ctx.getTenantIntegration().getId());
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Meta access token not configured");
        }
        return token;
    }
}
