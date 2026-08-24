package com.project.www.integrations.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.entity.IntegrationCredential;
import com.project.www.integrations.repository.IntegrationCredentialRepository;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.EncryptionService;
import com.project.www.integrations.service.IntegrationCredentialService;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.TenantContextService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthTokenRefreshScheduler {

    private final IntegrationCredentialRepository credentialRepository;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final IntegrationCredentialService credentialService;
    private final EncryptionService encryptionService;
    private final IntegrationLogService logService;
    private final TenantContextService tenantContextService;
    private final RestTemplate restTemplate;@Scheduled(fixedRate = 3600000)
    public void refreshExpiringTokens() {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(30);
        List<IntegrationCredential> expiring = credentialRepository.findAll().stream()
                .filter(c -> c.getTokenExpiry() != null && c.getTokenExpiry().isBefore(threshold))
                .filter(c -> c.getRefreshTokenEncrypted() != null)
                .toList();

        for (IntegrationCredential cred : expiring) {
            tenantIntegrationRepository.findById(cred.getTenantIntegrationId()).ifPresent(ti -> {
                Long tenantId = ti.getTenantId();
                try {
                    if ("GOOGLE".equals(ti.getCode())) {
                        refreshGoogle(cred, ti.getId(), tenantId);
                    } else if ("ZOOM".equals(ti.getCode())) {
                        refreshZoom(cred, ti.getId(), tenantId);
                    }
                } catch (Exception e) {
                    log.warn("Token refresh failed for {}: {}", ti.getCode(), e.getMessage());
                    logService.log(tenantId, ti.getId(), ti.getCode(), "token_refresh", "refresh",
                            null, null, "FAILED", 500, e.getMessage(), 0);
                }
            });
        }
    }

    private void refreshGoogle(IntegrationCredential cred, Long tiId, Long tenantId) {
        String clientId = credentialService.getDecryptedClientId(tiId);
        String clientSecret = credentialService.getDecryptedClientSecret(tiId);
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) return;
        String refreshToken = encryptionService.decrypt(cred.getRefreshTokenEncrypted());
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Map> response = restTemplate.exchange("https://oauth2.googleapis.com/token",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        if (response.getBody() != null) {
            String accessToken = (String) response.getBody().get("access_token");
            int expiresIn = response.getBody().get("expires_in") != null
                    ? ((Number) response.getBody().get("expires_in")).intValue() : 3600;
            credentialService.saveAccessToken(tiId, accessToken, refreshToken,
                    LocalDateTime.now().plusSeconds(expiresIn), cred.getScopes());
            logService.log(tenantId, tiId, "GOOGLE", "token_refresh", "refresh", null, null, "SUCCESS", 200, null, 0);
        }
    }

    private void refreshZoom(IntegrationCredential cred, Long tiId, Long tenantId) {
        String clientId = credentialService.getDecryptedClientId(tiId);
        String clientSecret = credentialService.getDecryptedClientSecret(tiId);
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) return;
        String refreshToken = encryptionService.decrypt(cred.getRefreshTokenEncrypted());
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Map> response = restTemplate.exchange("https://zoom.us/oauth/token",
                HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        if (response.getBody() != null) {
            String accessToken = (String) response.getBody().get("access_token");
            int expiresIn = response.getBody().get("expires_in") != null
                    ? ((Number) response.getBody().get("expires_in")).intValue() : 3600;
            credentialService.saveAccessToken(tiId, accessToken, refreshToken,
                    LocalDateTime.now().plusSeconds(expiresIn), "zoom");
            logService.log(tenantId, tiId, "ZOOM", "token_refresh", "refresh", null, null, "SUCCESS", 200, null, 0);
        }
    }
}
