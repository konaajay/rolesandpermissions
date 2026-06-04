package com.project.www.integrations.service;

import java.util.List;

import com.project.www.integrations.dto.SyncHistoryResponse;

public interface IntegrationSyncHistoryService {

    void record(Long tenantId, Long tenantIntegrationId, String syncType, String status,
                String message, int processed, int success, int failed);

    List<SyncHistoryResponse> getSyncHistory(String integrationCode);
}
