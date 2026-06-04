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
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;
import com.project.www.integrations.util.OAuthUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private static final String GOOGLE_CODE = "GOOGLE";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPES = "https://www.googleapis.com/auth/gmail.send "
            + "https://www.googleapis.com/auth/calendar "
            + "https://www.googleapis.com/auth/drive.file "
            + "https://www.googleapis.com/auth/spreadsheets";

    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationCredentialService credentialService;
    private final IntegrationLogService logService;
    private final IntegrationSyncHistoryService syncHistoryService;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;
    private final RestTemplate restTemplate;

    @Value("${google.client.id:}")
    private String clientId;

    @Value("${google.client.secret:}")
    private String clientSecret;

    @Value("${google.redirect.uri:http://localhost:8080/api/integrations/google/oauth/callback}")
    private String redirectUri;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public OAuthConnectResponse buildConnectUrl() {
        // Build the OAuth consent URL using tenant-specific credentials.
        TenantIntegrationContext ctx = tenantIntegrationResolver.resolveContext(GOOGLE_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        String clientId = credentialService.getDecryptedClientId(ti.getId());
        String clientSecret = credentialService.getDecryptedClientSecret(ti.getId());
        String redirectUri = credentialService.getRedirectUri(ti.getId());
        String scopes = credentialService.getScopes(ti.getId());
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank() || redirectUri == null || redirectUri.isBlank()) {
            throw new CredentialMissingException("Google OAuth credentials are not configured for this tenant.");
        }
        if (scopes == null || scopes.isBlank()) {
            scopes = SCOPES; // fallback to default scopes
        }
        Long tenantId = tenantContextService.getCurrentTenantId();
        String state = tenantId + ":" + System.currentTimeMillis();
        String url = UriComponentsBuilder.fromUriString(AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
        return OAuthConnectResponse.builder().authUrl(url).build();
    }

    @Override
    @Transactional
    public String handleCallback(String authCode, String state) {
        // Use DB-stored credentials for token exchange
        Long tenantIdFromState = OAuthUtils.extractTenantId(state);
        TenantIntegrationContext ctx = tenantIntegrationResolver.resolveContext(tenantIdFromState, GOOGLE_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        String clientId = credentialService.getDecryptedClientId(ti.getId());
        String clientSecret = credentialService.getDecryptedClientSecret(ti.getId());
        String redirectUri = credentialService.getRedirectUri(ti.getId());
        if (clientId == null || clientSecret == null || redirectUri == null) {
            return frontendUrl + "/integrations?error=google_not_configured";
        }
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IntegrationException("Google token exchange failed");
        }

        Map<String, Object> tokenData = response.getBody();
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn = tokenData.get("expires_in") != null
                ? ((Number) tokenData.get("expires_in")).intValue() : 3600;

        // Save token and scopes (use stored scopes or default)
        String scopes = credentialService.getScopes(ti.getId());
        if (scopes == null || scopes.isBlank()) {
            scopes = SCOPES;
        }
        credentialService.saveAccessToken(ti.getId(), accessToken, refreshToken,
                LocalDateTime.now().plusSeconds(expiresIn), scopes);
        ti.setConnected(true);
        ti.setEnabled(true);
        ti.setStatus(IntegrationStatus.CONNECTED);
        ti.setHealth(IntegrationHealth.HEALTHY);
        ti.setConnectedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);

        logService.log(tenantIdFromState, ti.getId(), GOOGLE_CODE, "oauth_callback", "connect",
                null, JsonUtil.toJson(Map.of("status", "connected")), "SUCCESS", 200, null, 0);
        return frontendUrl + "/integrations?connected=google";
    }

    @Override
    @Transactional
    public IntegrationTestResponse testConnection() {
        String token = getValidAccessToken();
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Google is not connected. Complete OAuth first.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + token,
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        TenantIntegrationContext ctx = tenantIntegrationResolver.resolveContext(GOOGLE_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        boolean ok = response.getStatusCode().is2xxSuccessful();
        ti.setHealth(ok ? IntegrationHealth.HEALTHY : IntegrationHealth.ERROR);
        ti.setLastSyncedAt(LocalDateTime.now());
        tenantIntegrationRepository.save(ti);

        Long tenantId = tenantContextService.getCurrentTenantId();
        syncHistoryService.record(tenantId, ti.getId(), "test", ok ? "Success" : "Failed",
                ok ? "Google token validated" : "Token validation failed", 1, ok ? 1 : 0, ok ? 0 : 1);
        logService.log(tenantId, ti.getId(), GOOGLE_CODE, "test", "test", null,
                JsonUtil.toJson(response.getBody()), ok ? "SUCCESS" : "FAILED",
                response.getStatusCode().value(), null, 0);

        return IntegrationTestResponse.builder()
                .health(ok ? IntegrationHealth.HEALTHY.name() : IntegrationHealth.ERROR.name())
                .status(ok ? IntegrationStatus.CONNECTED.name() : IntegrationStatus.FAILED.name())
                .build();
    }

    @Override
    @Transactional
    public String getValidAccessToken() {
        TenantIntegrationContext ctx = tenantIntegrationResolver.resolveContext(GOOGLE_CODE);
        TenantIntegration ti = ctx.getTenantIntegration();
        Long tenantIntegrationId = ti.getId();

        String accessToken = credentialService.getDecryptedAccessToken(tenantIntegrationId);
        String refreshToken = credentialService.getDecryptedRefreshToken(tenantIntegrationId);
        LocalDateTime tokenExpiry = credentialService.getTokenExpiry(tenantIntegrationId);

        if (accessToken == null || accessToken.isBlank()) {
            throw new CredentialMissingException("Google access token missing. Complete OAuth again.");
        }

        boolean nearExpiry = tokenExpiry == null || tokenExpiry.isBefore(LocalDateTime.now().plusMinutes(2));
        if (!nearExpiry) {
            return accessToken;
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CredentialMissingException("Google refresh token missing. Reconnect Google integration.");
        }

        String clientId = credentialService.getDecryptedClientId(tenantIntegrationId);
        String clientSecret = credentialService.getDecryptedClientSecret(tenantIntegrationId);
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new CredentialMissingException("Google client credentials missing. Configure Google again.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IntegrationException("Google token refresh failed");
        }

        Map<String, Object> tokenData = response.getBody();
        String newAccessToken = (String) tokenData.get("access_token");
        Integer expiresIn = tokenData.get("expires_in") != null
                ? ((Number) tokenData.get("expires_in")).intValue()
                : 3600;
        String scopes = credentialService.getScopes(tenantIntegrationId);
        credentialService.saveAccessToken(
                tenantIntegrationId,
                newAccessToken,
                refreshToken,
                LocalDateTime.now().plusSeconds(expiresIn),
                scopes
        );
        return newAccessToken;
    }
}
