package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.dto.SyncHistoryResponse;
import com.project.www.integrations.entity.IntegrationSyncHistory;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.exception.IntegrationNotFoundException;
import com.project.www.integrations.repository.IntegrationSyncHistoryRepository;
import com.project.www.integrations.repository.TenantIntegrationRepository;
import com.project.www.integrations.service.IntegrationSyncHistoryService;
import com.project.www.integrations.service.TenantContextService;
import com.project.www.integrations.util.DateFormatUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationSyncHistoryServiceImpl implements IntegrationSyncHistoryService {

    private final IntegrationSyncHistoryRepository syncHistoryRepository;
    private final TenantIntegrationRepository tenantIntegrationRepository;
    private final TenantContextService tenantContextService;

    @Override
    @Transactional
    public void record(Long tenantId, Long tenantIntegrationId, String syncType, String status,
                       String message, int processed, int success, int failed) {
        LocalDateTime now = LocalDateTime.now();
        syncHistoryRepository.save(IntegrationSyncHistory.builder()
                .tenantId(tenantId)
                .tenantIntegrationId(tenantIntegrationId)
                .syncType(syncType)
                .status(status)
                .message(message)
                .startedAt(now)
                .completedAt(now)
                .recordsProcessed(processed)
                .recordsSuccess(success)
                .recordsFailed(failed)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncHistoryResponse> getSyncHistory(String integrationCode) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        TenantIntegration ti = tenantIntegrationRepository.findByTenantIdAndCode(tenantId, integrationCode.toUpperCase())
                .orElseThrow(() -> new IntegrationNotFoundException("Integration not found: " + integrationCode));

        return syncHistoryRepository.findByTenantIntegrationIdOrderByStartedAtDesc(ti.getId())
                .stream()
                .map(h -> SyncHistoryResponse.builder()
                        .date(DateFormatUtil.formatDisplay(h.getCompletedAt() != null ? h.getCompletedAt() : h.getStartedAt()))
                        .status(h.getStatus())
                        .message(h.getMessage())
                        .build())
                .collect(Collectors.toList());
    }
}
