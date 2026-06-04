package com.project.www.integrations.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.www.integrations.dto.IntegrationLogResponse;

public interface IntegrationLogService {

    void log(Long tenantId, Long tenantIntegrationId, String integrationCode, String eventName,
             String action, String requestPayload, String responsePayload, String status,
             Integer httpStatus, String errorMessage, int retryCount);

    Page<IntegrationLogResponse> getLogs(String integrationCode, Pageable pageable);
}
