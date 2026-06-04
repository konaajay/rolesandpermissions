package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.entity.IntegrationDefinition;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.exception.IntegrationConfigurationException;
import com.project.www.integrations.repository.IntegrationDefinitionRepository;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.DateFormatUtil;
import com.project.www.integrations.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    private final IntegrationDefinitionRepository definitionRepository;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final TenantContextService tenantContextService;
    private final IntegrationCredentialService credentialService;
    private final IntegrationSettingService settingService;
    private final IntegrationLogService logService;
    private final IntegrationSyncHistoryService syncHistoryService;
    private final IntegrationDisconnectService integrationDisconnectService;
    private final GoogleOAuthService googleOAuthService;
    private final ZoomService zoomService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationCardResponse> getAllIntegrations() {
        return definitionRepository.findByActiveTrue().stream()
                .map(def -> toCard(def, tenantIntegrationResolver.findOrDefault(def)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IntegrationDetailsResponse getIntegrationDetails(String code) {
        TenantIntegrationContext ctx = resolveContext(code);
        return toDetails(ctx);
    }

    @Override
    @Transactional
    public IntegrationDetailsResponse toggleIntegration(String code, IntegrationToggleRequest request) {
        TenantIntegrationContext ctx = resolveContext(code);
        TenantIntegration ti = ctx.getTenantIntegration();
        ti.setEnabled(request.getEnabled());
        if (!request.getEnabled()) {
            ti.setHealth(IntegrationHealth.UNKNOWN);
        }
        tenantIntegrationRepository.save(ti);

        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ti.getId(), code.toUpperCase(), "toggle",
                request.getEnabled() ? "enable" : "disable", null, null, "SUCCESS", 200, null, 0);
        return toDetails(ctx);
    }

    @Override
    @Transactional
    public IntegrationTestResponse testIntegration(String code) {
        String upper = code.toUpperCase();
        return switch (upper) {
            case "GOOGLE" -> googleOAuthService.testConnection();
            case "ZOOM" -> zoomService.testConnection();
            default -> genericTest(upper);
        };
    }

    @Override
    @Transactional
    public IntegrationDetailsResponse configureIntegration(String code, IntegrationConfigureRequest request) {
        TenantIntegrationContext ctx = resolveContext(code);
        TenantIntegration ti = ctx.getTenantIntegration();

        if ("GOOGLE".equalsIgnoreCase(code)) {
            if (request.getClientId() == null || request.getClientId().isBlank()) {
                throw new IntegrationConfigurationException("Google clientId is required");
            }
            if (request.getClientSecret() == null || request.getClientSecret().isBlank()) {
                throw new IntegrationConfigurationException("Google clientSecret is required");
            }
            if (request.getRedirectUri() == null || request.getRedirectUri().isBlank()) {
                throw new IntegrationConfigurationException("Google redirectUri is required");
            }

            String scopesJoined = (request.getScopes() != null && !request.getScopes().isEmpty())
                    ? String.join(" ", request.getScopes())
                    : "openid email profile https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.send";

            credentialService.saveOAuthCredentials(
                    ti.getId(),
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getRedirectUri(),
                    scopesJoined);

            ti.setEnabled(true);
            ti.setConnected(false);
            ti.setStatus(IntegrationStatus.PENDING);
            ti.setHealth(IntegrationHealth.UNKNOWN);
            ti.setConnectedAt(null);
        } else if ("ZOOM".equalsIgnoreCase(code)) {
            if (request.getClientId() == null || request.getClientId().isBlank()) {
                throw new IntegrationConfigurationException("Zoom clientId is required");
            }
            if (request.getClientSecret() == null || request.getClientSecret().isBlank()) {
                throw new IntegrationConfigurationException("Zoom clientSecret is required");
            }
            if (request.getRedirectUri() == null || request.getRedirectUri().isBlank()) {
                throw new IntegrationConfigurationException("Zoom redirectUri is required");
            }

            String scopesJoined = (request.getScopes() != null && !request.getScopes().isEmpty())
                    ? String.join(" ", request.getScopes())
                    : "";

            credentialService.saveOAuthCredentials(
                    ti.getId(),
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getRedirectUri(),
                    scopesJoined);

            // Clear any stale tokens
            credentialService.clearTokens(ti.getId());

            ti.setEnabled(true);
            ti.setConnected(false);
            ti.setStatus(IntegrationStatus.PENDING);
            ti.setHealth(IntegrationHealth.UNKNOWN);
            ti.setConnectedAt(null);
        } else {
            credentialService.saveApiKeySecret(ti.getId(), request.getApiKey(), request.getApiSecret());

            boolean hasCreds = request.getApiKey() != null || request.getApiSecret() != null
                    || credentialService.findByTenantIntegrationId(ti.getId()).isPresent();

            ti.setStatus(hasCreds ? IntegrationStatus.CONNECTED : IntegrationStatus.PENDING);
            ti.setConnected(hasCreds);

            if (hasCreds) {
                ti.setConnectedAt(LocalDateTime.now());
                ti.setHealth(IntegrationHealth.HEALTHY);
            }
        }

        if (request.getWebhookUrl() != null) {
            settingService.saveSetting(ti.getId(), "webhook_url", request.getWebhookUrl(), false);
        }

        String environment = request.getEnvironment();

        if (environment == null || environment.isBlank()) {
            environment = "development";
        }

        ti.setEnvironment(environment.toLowerCase());
        settingService.saveSetting(ti.getId(), "environment", environment.toLowerCase(), false);

        if (request.getSettings() != null) {
            settingService.saveSettings(ti.getId(), request.getSettings());
        }

        tenantIntegrationRepository.save(ti);

        Long tenantId = tenantContextService.getCurrentTenantId();
        Map<String, Object> logPayload = new java.util.HashMap<>();
        logPayload.put("environment", request.getEnvironment());
        logPayload.put("code", code.toUpperCase());
        logService.log(
                tenantId,
                ti.getId(),
                code.toUpperCase(),
                "configure",
                "configure",
                JsonUtil.toJson(logPayload),
                null,
                "SUCCESS",
                200,
                null,
                0
        );

        return toDetails(ctx);
    }

    private IntegrationTestResponse genericTest(String code) {
        TenantIntegrationContext ctx = resolveContext(code);
        TenantIntegration ti = ctx.getTenantIntegration();
        boolean hasCred = credentialService.findByTenantIntegrationId(ti.getId()).isPresent();
        boolean hasSettings = !settingService.getAllSettings(ti.getId()).isEmpty();

        if (!hasCred && !hasSettings) {
            ti.setHealth(IntegrationHealth.ERROR);
            ti.setStatus(IntegrationStatus.FAILED);
            tenantIntegrationRepository.save(ti);
            Long tenantId = tenantContextService.getCurrentTenantId();
            logService.log(tenantId, ti.getId(), code, "test", "test", null, null, "FAILED", 400,
                    "Credentials or settings missing", 0);
            throw new com.project.www.integrations.exception.CredentialMissingException(
                    "Credentials or settings missing for " + code);
        }

        ti.setHealth(IntegrationHealth.HEALTHY);
        ti.setStatus(IntegrationStatus.CONNECTED);
        ti.setConnected(true);
        ti.setLastSyncedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);

        Long tenantId = tenantContextService.getCurrentTenantId();
        syncHistoryService.record(tenantId, ti.getId(), "test", "Success", "Connection test successful", 1, 1, 0);
        logService.log(tenantId, ti.getId(), code, "test", "test", null, "{\"ok\":true}", "SUCCESS", 200, null, 0);

        return IntegrationTestResponse.builder()
                .health(IntegrationHealth.HEALTHY.name())
                .status(IntegrationStatus.CONNECTED.name())
                .build();
    }

    @Override
    @Transactional
    public void disconnectIntegration(String code) {
        integrationDisconnectService.disconnect(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IntegrationLogResponse> getIntegrationLogs(String code, Pageable pageable) {
        return logService.getLogs(code.toUpperCase(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncHistoryResponse> getSyncHistory(String code) {
        return syncHistoryService.getSyncHistory(code);
    }

    @Override
    public OAuthConnectResponse getOAuthConnectUrl(String code) {
        return switch (code.toUpperCase()) {
            case "GOOGLE" -> googleOAuthService.buildConnectUrl();
            case "ZOOM" -> zoomService.buildConnectUrl();
            default -> OAuthConnectResponse.builder()
                    .authUrl("/api/integrations/" + code.toUpperCase() + "/oauth/connect")
                    .build();
        };
    }

    @Override
    public String handleOAuthCallback(String code, String authCode, String state) {
        return switch (code.toUpperCase()) {
            case "GOOGLE" -> googleOAuthService.handleCallback(authCode, state);
            case "ZOOM" -> zoomService.handleCallback(authCode, state);
            default -> frontendUrl + "/integrations?error=unsupported_oauth";
        };
    }

    @Override
    @Transactional
    public TenantIntegrationContext resolveContext(String code) {
        return tenantIntegrationResolver.resolveContext(code);
    }

    private IntegrationCardResponse toCard(IntegrationDefinition def, TenantIntegration ti) {
        return IntegrationCardResponse.builder()
                .id(def.getCode())
                .code(def.getCode())
                .name(def.getName())
                .description(def.getDescription())
                .color(def.getColor())
                .enabled(Boolean.TRUE.equals(ti.getEnabled()))
                .connected(Boolean.TRUE.equals(ti.getConnected()))
                .health(ti.getHealth() != null ? ti.getHealth().name().toLowerCase() : "unknown")
                .lastSynced(DateFormatUtil.formatDisplay(ti.getLastSyncedAt()))
                .environment(ti.getEnvironment())
                .build();
    }

    private IntegrationDetailsResponse toDetails(TenantIntegrationContext ctx) {
        TenantIntegration ti = ctx.getTenantIntegration();
        IntegrationDefinition def = ctx.getDefinition();

        String apiKeyMasked;
        String apiSecretMasked;

        if ("GOOGLE".equalsIgnoreCase(def.getCode())) {
            apiKeyMasked = credentialService.getMaskedClientId(ti.getId());
            apiSecretMasked = "************";
        } else {
            apiKeyMasked = credentialService.getMaskedApiKey(ti.getId());
            apiSecretMasked = credentialService.getMaskedApiSecret(ti.getId());
        }

        return IntegrationDetailsResponse.builder()
                .code(def.getCode())
                .name(def.getName())
                .description(def.getDescription())
                .enabled(Boolean.TRUE.equals(ti.getEnabled()))
                .connected(Boolean.TRUE.equals(ti.getConnected()))
                .health(ti.getHealth() != null ? ti.getHealth().name().toLowerCase() : "unknown")
                .environment(ti.getEnvironment())
                .webhookUrl(settingService.getSettingOrDefault(ti.getId(), "webhook_url", null))
                .apiKeyMasked(apiKeyMasked)
                .apiSecretMasked(apiSecretMasked)
                .lastSynced(DateFormatUtil.formatDisplay(ti.getLastSyncedAt()))
                .build();
    }

}
