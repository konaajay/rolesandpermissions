package com.project.www.integrations.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.project.www.integrations.entity.IntegrationCredential;

public interface IntegrationCredentialService {

    IntegrationCredential getOrCreate(Long tenantIntegrationId);

    // Generic API key/secret providers
    void saveApiKeySecret(Long tenantIntegrationId, String apiKey, String apiSecret);

    // Providers like Cashfree/Zoom that use clientId/clientSecret only
    void saveClientCredentials(Long tenantIntegrationId, String clientId, String clientSecret);

    // Generic OAuth credentials (clientId, clientSecret, redirectUri, scopes)
    void saveOAuthCredentials(
            Long tenantIntegrationId,
            String clientId,
            String clientSecret,
            String redirectUri,
            String scopes
    );

    void saveAccessToken(
            Long tenantIntegrationId,
            String accessToken,
            String refreshToken,
            LocalDateTime expiry,
            String scopes
    );

    void clearTokens(Long tenantIntegrationId);

    Optional<IntegrationCredential> findByTenantIntegrationId(Long tenantIntegrationId);

    String getDecryptedAccessToken(Long tenantIntegrationId);

    String getMaskedApiKey(Long tenantIntegrationId);

    String getMaskedApiSecret(Long tenantIntegrationId);

    String getDecryptedClientId(Long tenantIntegrationId);
    String getDecryptedClientSecret(Long tenantIntegrationId);

    String getDecryptedRefreshToken(Long tenantIntegrationId);

    LocalDateTime getTokenExpiry(Long tenantIntegrationId);

    String getMaskedClientId(Long tenantIntegrationId);

    String getRedirectUri(Long tenantIntegrationId);

    String getScopes(Long tenantIntegrationId);
}
