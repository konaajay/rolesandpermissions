package com.project.www.integrations.service.impl;

import com.project.www.accessmanagement.entity.Permission;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.dto.ApiKeyCreateRequest;
import com.project.www.integrations.dto.ApiKeyResponse;
import com.project.www.integrations.dto.ApiKeyUsageLogResponse;
import com.project.www.integrations.entity.ApiKey;
import com.project.www.integrations.entity.ApiKeyUsageLog;
import com.project.www.integrations.enums.ApiKeyStatus;
import com.project.www.integrations.exception.IntegrationNotFoundException;
import com.project.www.integrations.repository.ApiKeyRepository;
import com.project.www.integrations.repository.ApiKeyUsageLogRepository;
import com.project.www.integrations.service.ApiKeyService;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.TenantContextService;
import com.project.www.integrations.util.ApiKeyGenerator;
import com.project.www.integrations.util.DateFormatUtil;
import com.project.www.integrations.util.JsonUtil;
import com.project.www.integrations.util.TokenMaskingUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyUsageLogRepository usageLogRepository;
    private final TenantContextService tenantContextService;
    private final IntegrationLogService logService;

    @Override
    @Transactional
    public ApiKeyResponse create(ApiKeyCreateRequest request) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        String plainKey = ApiKeyGenerator.generateApiKey();
        String plainSecret = ApiKeyGenerator.generateApiSecret();

        ApiKey apiKey = ApiKey.builder()
                .tenantId(tenantId)
                .keyName(request.getKeyName())
                .apiKeyHash(ApiKeyGenerator.hash(plainKey))
                .apiSecretHash(ApiKeyGenerator.hash(plainSecret))
                .maskedKey(TokenMaskingUtil.mask(plainKey))
                .permissions(request.getPermissions() != null ? String.join(",", request.getPermissions()) : "")
                .ipWhitelist(request.getIpWhitelist() != null ? String.join(",", request.getIpWhitelist()) : "")
                .expiryDate(request.getExpiryDate())
                .status(ApiKeyStatus.ACTIVE)
                .createdBy(tenantContextService.getCurrentUserId())
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        logService.log(tenantId, null, "API_KEY", "api_key_created", "create",
                JsonUtil.toJson(request), "{\"id\":" + apiKey.getId() + "}", "SUCCESS", 200, null, 0);

        ApiKeyResponse response = toResponse(apiKey);
        response.setApiKey(plainKey);
        response.setApiSecret(plainSecret);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> list() {
        Long tenantId = tenantContextService.getCurrentTenantId();
        return apiKeyRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiKeyResponse update(Long id, ApiKeyCreateRequest request) {
        ApiKey apiKey = findKey(id);
        apiKey.setKeyName(request.getKeyName());
        if (request.getPermissions() != null) {
            apiKey.setPermissions(String.join(",", request.getPermissions()));
        }
        if (request.getIpWhitelist() != null) {
            apiKey.setIpWhitelist(String.join(",", request.getIpWhitelist()));
        }
        apiKey.setExpiryDate(request.getExpiryDate());
        return toResponse(apiKeyRepository.save(apiKey));
    }

    @Override
    @Transactional
    public ApiKeyResponse regenerate(Long id) {
        ApiKey apiKey = findKey(id);
        String plainKey = ApiKeyGenerator.generateApiKey();
        String plainSecret = ApiKeyGenerator.generateApiSecret();
        apiKey.setApiKeyHash(ApiKeyGenerator.hash(plainKey));
        apiKey.setApiSecretHash(ApiKeyGenerator.hash(plainSecret));
        apiKey.setMaskedKey(TokenMaskingUtil.mask(plainKey));
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setRevokedAt(null);
        apiKey = apiKeyRepository.save(apiKey);

        ApiKeyResponse response = toResponse(apiKey);
        response.setApiKey(plainKey);
        response.setApiSecret(plainSecret);
        return response;
    }

    @Override
    @Transactional
    public void revoke(Long id) {
        ApiKey apiKey = findKey(id);
        apiKey.setStatus(ApiKeyStatus.REVOKED);
        apiKey.setRevokedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);
        Long tenantId = tenantContextService.getCurrentTenantId();
        logService.log(tenantId, null, "API_KEY", "api_key_revoked", "revoke",
                "{\"id\":" + id + "}", null, "SUCCESS", 200, null, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApiKeyUsageLogResponse> getUsageLogs(Long id, Pageable pageable) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        findKey(id);
        return usageLogRepository.findByTenantIdAndApiKeyIdOrderByCreatedAtDesc(tenantId, id, pageable)
                .map(log -> ApiKeyUsageLogResponse.builder()
                        .date(DateFormatUtil.formatDisplay(log.getCreatedAt()))
                        .endpoint(log.getEndpoint())
                        .method(log.getMethod())
                        .ipAddress(log.getIpAddress())
                        .status(log.getStatus())
                        .build());
    }

    private ApiKey findKey(Long id) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        return apiKeyRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IntegrationNotFoundException("API key not found"));
    }

    private ApiKeyResponse toResponse(ApiKey apiKey) {
        List<String> permissions = apiKey.getPermissions() != null && !apiKey.getPermissions().isBlank()
                ? Arrays.asList(apiKey.getPermissions().split(",")) : List.of();
        List<String> ipWhitelist = apiKey.getIpWhitelist() != null && !apiKey.getIpWhitelist().isBlank()
                ? Arrays.asList(apiKey.getIpWhitelist().split(",")) : List.of();
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .keyName(apiKey.getKeyName())
                .maskedKey(apiKey.getMaskedKey())
                .permissions(permissions)
                .ipWhitelist(ipWhitelist)
                .expiryDate(apiKey.getExpiryDate())
                .status(apiKey.getStatus() != null ? apiKey.getStatus().name() : null)
                .createdAt(apiKey.getCreatedAt())
                .build();
    }
    @Override
    public ApiKey validateExternalApiKey(String apiKey, String apiSecret, String permission, String endpoint, String method, String ipAddress) {
        // Missing API key
        if (apiKey == null || apiKey.isBlank()) {
            logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{apiKey missing}", null, "MISSING_API_KEY", 401, null, 0);
            throw new IntegrationNotFoundException("Missing API key");
        }
        // Missing API secret
        if (apiSecret == null || apiSecret.isBlank()) {
            logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{apiSecret missing}", null, "MISSING_API_SECRET", 401, null, 0);
            throw new IntegrationNotFoundException("Missing API secret");
        }
        String apiKeyHash = ApiKeyGenerator.hash(apiKey);
        ApiKey key = apiKeyRepository.findByApiKeyHashAndStatus(apiKeyHash, ApiKeyStatus.ACTIVE)
                .orElseThrow(() -> {
                    logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{invalid key}", null, "INVALID_API_KEY", 401, null, 0);
                    return new IntegrationNotFoundException("Invalid API key");
                });
        // Validate secret
        String apiSecretHash = ApiKeyGenerator.hash(apiSecret);
        if (!apiSecretHash.equals(key.getApiSecretHash())) {
            logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{invalid secret}", null, "INVALID_API_SECRET", 401, null, 0);
            throw new IntegrationNotFoundException("Invalid API secret");
        }
        // Validate expiry
        if (key.getExpiryDate() != null && LocalDateTime.now().isAfter(key.getExpiryDate())) {
            key.setStatus(ApiKeyStatus.EXPIRED);
            apiKeyRepository.save(key);
            logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{expired}", null, "EXPIRED", 401, null, 0);
            throw new IntegrationNotFoundException("API key expired");
        }
        // Validate IP whitelist if provided
        if (ipAddress != null && !ipAddress.isBlank() && key.getIpWhitelist() != null && !key.getIpWhitelist().isBlank()) {
            List<String> whitelist = Arrays.asList(key.getIpWhitelist().split(","));
            if (!whitelist.contains(ipAddress)) {
                logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{ip blocked}", null, "IP_BLOCKED", 401, null, 0);
                throw new IntegrationNotFoundException("IP not allowed");
            }
        }
        // Validate permission if required
        if (permission != null && !permission.isBlank() && key.getPermissions() != null && !key.getPermissions().isBlank()) {
            List<String> perms = Arrays.asList(key.getPermissions().split(","));
            if (!perms.contains(permission)) {
                logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{permission denied}", null, "PERMISSION_DENIED", 401, null, 0);
                throw new IntegrationNotFoundException("Permission denied");
            }
        }
        // Successful validation, log usage
        ApiKeyUsageLog log = ApiKeyUsageLog.builder()
                .tenantId(tenantContextService.getCurrentTenantId())
                .apiKeyId(key.getId())
                .endpoint(endpoint)
                .method(method)
                .ipAddress(ipAddress)
                .status("SUCCESS")
                .build();
        usageLogRepository.save(log);
        logService.log(tenantContextService.getCurrentTenantId(), null, "API_KEY", "api_key_validation", "validate", "{success}", null, "SUCCESS", 200, null, 0);
        return key;
    }
}
