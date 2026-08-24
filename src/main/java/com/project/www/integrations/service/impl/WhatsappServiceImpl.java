package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.IntegrationTestResponse;
import com.project.www.integrations.dto.WhatsappConfigureRequest;
import com.project.www.integrations.dto.WhatsappSendRequest;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhatsappServiceImpl implements WhatsappService {

    private static final String WHATSAPP_CODE = "WHATSAPP";

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationDisconnectService integrationDisconnectService;
    private final IntegrationCredentialService credentialService;
    private final IntegrationSettingService settingService;
    private final IntegrationLogService logService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;
    private final RestTemplate restTemplate;

    @Override
    @Transactional("integrationTransactionManager")
    public void configure(WhatsappConfigureRequest request) {
        var ctx = tenantIntegrationResolver.resolveContext(WHATSAPP_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        credentialService.saveAccessToken(ti.getId(), request.getAccessToken(), null, null, "whatsapp");
        settingService.saveSetting(ti.getId(), "phone_number_id", request.getPhoneNumberId(), false);
        if (request.getBusinessAccountId() != null) {
            settingService.saveSetting(ti.getId(), "business_account_id", request.getBusinessAccountId(), false);
        }
        if (request.getWebhookVerifyToken() != null) {
            settingService.saveSetting(ti.getId(), "verify_token", request.getWebhookVerifyToken(), true);
        }
        if (request.getDefaultCountryCode() != null) {
            settingService.saveSetting(ti.getId(), "default_country_code", request.getDefaultCountryCode(), false);
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
        logService.log(tenantId, ti.getId(), WHATSAPP_CODE, "configure", "configure", null, null, "SUCCESS", 200, null, 0);
    }

    @Override
@Transactional("integrationTransactionManager")
    public Map<String, Object> getStatus() {
        var ctx = tenantIntegrationResolver.resolveContext(WHATSAPP_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        Map<String, Object> status = new HashMap<>();
        status.put("connected", ti.getConnected());
        status.put("phoneNumberId", settingService.getSetting(ti.getId(), "phone_number_id").orElse(null));
        return status;
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void sendMessage(WhatsappSendRequest request) {
        var ctx = tenantIntegrationResolver.resolveContext(WHATSAPP_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        String token = credentialService.getDecryptedAccessToken(ti.getId());
        String phoneNumberId = settingService.getSetting(ti.getId(), "phone_number_id")
                .orElseThrow(() -> new CredentialMissingException("WhatsApp phone_number_id not configured"));

        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("WhatsApp access token not configured");
        }

        String url = "https://graph.facebook.com/v19.0/" + phoneNumberId + "/messages";
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", request.getTo().replace("+", ""),
                "type", "text",
                "text", Map.of("body", request.getMessage())
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ti.getId(), WHATSAPP_CODE, "send_message", "send",
                JsonUtil.toJson(request), JsonUtil.toJson(response.getBody()),
                response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED",
                response.getStatusCode().value(), null, 0);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IntegrationException("WhatsApp API returned error: " + response.getStatusCode());
        }
    }

    @Override
    @Transactional("integrationTransactionManager")
    public IntegrationTestResponse testConnection() {
        var ctx = tenantIntegrationResolver.resolveContext(WHATSAPP_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        boolean ok = credentialService.getDecryptedAccessToken(ti.getId()) != null
                && settingService.getSetting(ti.getId(), "phone_number_id").isPresent();
        ti.setHealth(ok ? IntegrationHealth.HEALTHY : IntegrationHealth.ERROR);
        tenantIntegrationRepository.save(ti);
        if (!ok) {
            throw new CredentialMissingException("WhatsApp credentials incomplete");
        }
        return IntegrationTestResponse.builder()
                .health(IntegrationHealth.HEALTHY.name())
                .status(IntegrationStatus.CONNECTED.name())
                .build();
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void disconnect() {
        integrationDisconnectService.disconnect(WHATSAPP_CODE);
    }
}
