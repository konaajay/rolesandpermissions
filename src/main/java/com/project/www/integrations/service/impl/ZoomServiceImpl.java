package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.www.integrations.dto.IntegrationTestResponse;
import com.project.www.integrations.dto.OAuthConnectResponse;
import com.project.www.integrations.dto.ZoomMeetingRequest;
import com.project.www.integrations.entity.ExternalEventMapping;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.ExternalEventMappingRepository;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;
import com.project.www.integrations.util.OAuthUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZoomServiceImpl implements ZoomService {

    private static final String ZOOM_CODE = "ZOOM";

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationDisconnectService integrationDisconnectService;
    private final IntegrationCredentialService credentialService;
    private final IntegrationLogService logService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;
    private final ExternalEventMappingRepository eventMappingRepository;
    private final RestTemplate restTemplate;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public OAuthConnectResponse buildConnectUrl() {
        var ctx = tenantIntegrationResolver.resolveContext(ZOOM_CODE);
        Long tenantId = tenantContextService.getCurrentTenantId();
        String clientId = credentialService.getDecryptedClientId(ctx.getTenantIntegration().getId());
        String redirectUri = credentialService.getRedirectUri(ctx.getTenantIntegration().getId());
        if (clientId == null || clientId.isBlank()) {
            throw new CredentialMissingException("Zoom client ID not configured");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new CredentialMissingException("Zoom redirect URI not configured");
        }
        String state = tenantId + ":" + System.currentTimeMillis();
        String url = UriComponentsBuilder.fromUriString("https://zoom.us/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build(true)
                .toUriString();
        return OAuthConnectResponse.builder().authUrl(url).build();
    }

    @Override
    @Transactional
    public String handleCallback(String authCode, String state) {
        Long tenantIdFromState = OAuthUtils.extractTenantId(state);
        var ctx = tenantIntegrationResolver.resolveContext(tenantIdFromState, ZOOM_CODE);
        Long tiId = ctx.getTenantIntegration().getId();
        String clientId = credentialService.getDecryptedClientId(tiId);
        String clientSecret = credentialService.getDecryptedClientSecret(tiId);
        String redirectUri = credentialService.getRedirectUri(tiId);
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank() || redirectUri == null || redirectUri.isBlank()) {
            return frontendUrl + "/integrations?error=zoom_not_configured";
        }
        try {
            String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + basic);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", authCode);
            body.add("redirect_uri", redirectUri);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://zoom.us/oauth/token", HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IntegrationException("Zoom token exchange failed");
            }

            Map<String, Object> tokenData = response.getBody();
            String accessToken = (String) tokenData.get("access_token");
            String refreshToken = (String) tokenData.get("refresh_token");
            int expiresIn = tokenData.get("expires_in") != null ? ((Number) tokenData.get("expires_in")).intValue() : 3600;

            TenantIntegration ti = ctx.getTenantIntegration();
            // Use scopes stored in DB if present
            String scopes = credentialService.getScopes(ti.getId());
            credentialService.saveAccessToken(ti.getId(), accessToken, refreshToken,
                    LocalDateTime.now().plusSeconds(expiresIn), scopes);
            ti.setConnected(true);
            ti.setEnabled(true);
            ti.setStatus(IntegrationStatus.CONNECTED);
            ti.setHealth(IntegrationHealth.HEALTHY);
            ti.setConnectedAt(LocalDateTime.now());
            tenantIntegrationRepository.save(ti);

            logService.log(tenantIdFromState, ti.getId(), ZOOM_CODE, "oauth_callback", "connect",
                    null, null, "SUCCESS", 200, null, 0);
            return frontendUrl + "/integrations?connected=zoom";
        } catch (Exception e) {
            return frontendUrl + "/integrations?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> getStatus() {
        var ctx = tenantIntegrationResolver.resolveContext(ZOOM_CODE);
        return Map.of(
                "configured", credentialService.findByTenantIntegrationId(ctx.getTenantIntegration().getId()).isPresent(),
                "connected", Boolean.TRUE.equals(ctx.getTenantIntegration().getConnected()),
                "enabled", Boolean.TRUE.equals(ctx.getTenantIntegration().getEnabled()),
                "status", ctx.getTenantIntegration().getStatus() != null ? ctx.getTenantIntegration().getStatus().name() : "UNKNOWN",
                "health", ctx.getTenantIntegration().getHealth() != null ? ctx.getTenantIntegration().getHealth().name() : "UNKNOWN",
                "environment", ctx.getTenantIntegration().getEnvironment() != null ? ctx.getTenantIntegration().getEnvironment() : "development"
        );
    }

    @Override
    @Transactional
    public Map<String, Object> createMeeting(ZoomMeetingRequest request) {
        String token = getAccessToken();
        Map<String, Object> meeting = Map.of(
                "topic", request.getTopic(),
                "type", 2,
                "start_time", request.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                "duration", request.getDurationMinutes(),
                "timezone", request.getTimezone(),
                "agenda", request.getAgenda() != null ? request.getAgenda() : ""
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.zoom.us/v2/users/me/meetings", HttpMethod.POST,
                new HttpEntity<>(meeting, headers), Map.class);

        var ctx = tenantIntegrationResolver.resolveContext(ZOOM_CODE);
        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, ctx.getTenantIntegration().getId(), ZOOM_CODE, "meeting_created", "create",
                JsonUtil.toJson(request), JsonUtil.toJson(response.getBody()),
                response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED",
                response.getStatusCode().value(), null, 0);

        if (response.getBody() != null && response.getBody().get("id") != null) {
            eventMappingRepository.save(ExternalEventMapping.builder()
                    .tenantId(tenantId)
                    .provider("ZOOM")
                    .externalEventId(String.valueOf(response.getBody().get("id")))
                    .internalModule(request.getModule())
                    .internalReferenceId(request.getReferenceId())
                    .metadataJson(JsonUtil.toJson(response.getBody()))
                    .build());
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IntegrationException("Zoom meeting creation failed");
        }
        return response.getBody();
    }

    @Override
    @Transactional
    public Map<String, Object> updateMeeting(String meetingId, ZoomMeetingRequest request) {
        String token = getAccessToken();
        Map<String, Object> meeting = Map.of(
                "topic", request.getTopic(),
                "start_time", request.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                "duration", request.getDurationMinutes(),
                "timezone", request.getTimezone(),
                "agenda", request.getAgenda() != null ? request.getAgenda() : ""
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.zoom.us/v2/meetings/" + meetingId, HttpMethod.PATCH,
                new HttpEntity<>(meeting, headers), Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IntegrationException("Zoom meeting update failed");
        }
        return response.getBody();
    }

    @Override
    @Transactional
    public void deleteMeeting(String meetingId) {
        String token = getAccessToken();
        Long tenantId = tenantContextService.getCurrentTenantId();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        restTemplate.exchange("https://api.zoom.us/v2/meetings/" + meetingId, HttpMethod.DELETE,
                new HttpEntity<>(headers), Void.class);
        // Update external event mapping status to CANCELLED and set deletion timestamp
        eventMappingRepository.findByTenantIdAndProviderAndExternalEventId(tenantId, "ZOOM", meetingId)
                .ifPresent(mapping -> {
                    mapping.setStatus("CANCELLED");
                    mapping.setDeletedAt(LocalDateTime.now());
                    eventMappingRepository.save(mapping);
                });
    }

    @Override
    @Transactional
    public IntegrationTestResponse testConnection() {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.zoom.us/v2/users/me", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        boolean ok = response.getStatusCode().is2xxSuccessful();
        return IntegrationTestResponse.builder()
                .health(ok ? IntegrationHealth.HEALTHY.name() : IntegrationHealth.ERROR.name())
                .status(ok ? IntegrationStatus.CONNECTED.name() : IntegrationStatus.FAILED.name())
                .build();
    }

    @Override
    @Transactional
    public void disconnect() {
        integrationDisconnectService.disconnect(ZOOM_CODE);
    }

    private String getAccessToken() {
        var ctx = tenantIntegrationResolver.resolveContext(ZOOM_CODE);
        String token = credentialService.getDecryptedAccessToken(ctx.getTenantIntegration().getId());
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Zoom not connected. Complete OAuth first.");
        }
        return token;
    }
}
