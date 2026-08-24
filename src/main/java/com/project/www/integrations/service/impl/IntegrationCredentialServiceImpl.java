package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.entity.IntegrationCredential;
import com.project.www.integrations.repository.IntegrationCredentialRepository;
import com.project.www.integrations.service.EncryptionService;
import com.project.www.integrations.service.IntegrationCredentialService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IntegrationCredentialServiceImpl implements IntegrationCredentialService {

    private final IntegrationCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional("integrationTransactionManager")
    public IntegrationCredential getOrCreate(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .orElseGet(() -> credentialRepository.save(IntegrationCredential.builder()
                        .tenantIntegrationId(tenantIntegrationId)
                        .build()));
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void saveApiKeySecret(Long tenantIntegrationId, String apiKey, String apiSecret) {
        IntegrationCredential cred = getOrCreate(tenantIntegrationId);
        if (apiKey != null && !apiKey.isBlank()) {
            cred.setApiKeyEncrypted(encryptionService.encrypt(apiKey));
        }
        if (apiSecret != null && !apiSecret.isBlank()) {
            cred.setApiSecretEncrypted(encryptionService.encrypt(apiSecret));
        }
        credentialRepository.save(cred);
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void saveClientCredentials(Long tenantIntegrationId, String clientId, String clientSecret) {
        IntegrationCredential cred = getOrCreate(tenantIntegrationId);
        if (clientId != null && !clientId.isBlank()) {
            cred.setClientIdEncrypted(encryptionService.encrypt(clientId));
        }
        if (clientSecret != null && !clientSecret.isBlank()) {
            cred.setClientSecretEncrypted(encryptionService.encrypt(clientSecret));
        }
        credentialRepository.save(cred);
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void saveOAuthCredentials(Long tenantIntegrationId, String clientId, String clientSecret, String redirectUri, String scopes) {
        IntegrationCredential cred = getOrCreate(tenantIntegrationId);
        if (clientId != null && !clientId.isBlank()) {
            cred.setClientIdEncrypted(encryptionService.encrypt(clientId));
        }
        if (clientSecret != null && !clientSecret.isBlank()) {
            cred.setClientSecretEncrypted(encryptionService.encrypt(clientSecret));
        }
        if (redirectUri != null && !redirectUri.isBlank()) {
            cred.setRedirectUri(redirectUri);
        }
        if (scopes != null && !scopes.isBlank()) {
            cred.setScopes(scopes);
        }
        credentialRepository.save(cred);
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void saveAccessToken(Long tenantIntegrationId, String accessToken, String refreshToken, LocalDateTime expiry, String scopes) {
        IntegrationCredential cred = getOrCreate(tenantIntegrationId);
        if (accessToken != null) {
            cred.setAccessTokenEncrypted(encryptionService.encrypt(accessToken));
        }
        if (refreshToken != null) {
            cred.setRefreshTokenEncrypted(encryptionService.encrypt(refreshToken));
        }
        cred.setTokenExpiry(expiry);
        cred.setScopes(scopes);
        credentialRepository.save(cred);
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void clearTokens(Long tenantIntegrationId) {
        credentialRepository.findByTenantIntegrationId(tenantIntegrationId).ifPresent(cred -> {
            cred.setAccessTokenEncrypted(null);
            cred.setRefreshTokenEncrypted(null);
            cred.setTokenExpiry(null);
            credentialRepository.save(cred);
        });
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public Optional<IntegrationCredential> findByTenantIntegrationId(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getDecryptedRefreshToken(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> encryptionService.decrypt(c.getRefreshTokenEncrypted()))
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getDecryptedAccessToken(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> encryptionService.decrypt(c.getAccessTokenEncrypted()))
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public LocalDateTime getTokenExpiry(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(IntegrationCredential::getTokenExpiry)
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getMaskedApiKey(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> encryptionService.decrypt(c.getApiKeyEncrypted()))
                .map(encryptionService::mask)
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getMaskedApiSecret(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> c.getApiSecretEncrypted() != null ? "************" : null)
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getDecryptedClientId(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> encryptionService.decrypt(c.getClientIdEncrypted()))
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getDecryptedClientSecret(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> encryptionService.decrypt(c.getClientSecretEncrypted()))
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getMaskedClientId(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(c -> encryptionService.decrypt(c.getClientIdEncrypted()))
                .map(encryptionService::mask)
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getRedirectUri(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(IntegrationCredential::getRedirectUri)
                .orElse(null);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getScopes(Long tenantIntegrationId) {
        return credentialRepository.findByTenantIntegrationId(tenantIntegrationId)
                .map(IntegrationCredential::getScopes)
                .orElse(null);
    }
}
