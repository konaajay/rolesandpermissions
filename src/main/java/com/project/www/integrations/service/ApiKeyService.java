package com.project.www.integrations.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.www.integrations.dto.ApiKeyCreateRequest;
import com.project.www.integrations.dto.ApiKeyResponse;
import com.project.www.integrations.dto.ApiKeyUsageLogResponse;
import com.project.www.integrations.entity.ApiKey;

import java.util.List;

public interface ApiKeyService {
    ApiKeyResponse create(ApiKeyCreateRequest request);
    List<ApiKeyResponse> list();
    ApiKeyResponse update(Long id, ApiKeyCreateRequest request);
    ApiKeyResponse regenerate(Long id);
    void revoke(Long id);
    Page<ApiKeyUsageLogResponse> getUsageLogs(Long id, Pageable pageable);
    ApiKey validateExternalApiKey(String apiKey, String apiSecret, String permission, String endpoint, String method, String ipAddress);
}
