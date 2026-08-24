package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.dto.IntegrationLogResponse;
import com.project.www.integrations.entity.IntegrationLog;
import com.project.www.integrations.repository.IntegrationLogRepository;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.TenantContextService;
import com.project.www.integrations.util.DateFormatUtil;

@Service
@RequiredArgsConstructor
public class IntegrationLogServiceImpl implements IntegrationLogService {

    private final IntegrationLogRepository logRepository;
    private final TenantContextService tenantContextService;

    @Override
    @Transactional("integrationTransactionManager")
    public void log(Long tenantId, Long tenantIntegrationId, String integrationCode, String eventName,
                    String action, String requestPayload, String responsePayload, String status,
                    Integer httpStatus, String errorMessage, int retryCount) {
        logRepository.save(IntegrationLog.builder()
                .tenantId(tenantId)
                .tenantIntegrationId(tenantIntegrationId)
                .integrationCode(integrationCode)
                .eventName(eventName)
                .action(action)
                .requestPayload(requestPayload)
                .responsePayload(responsePayload)
                .status(status)
                .httpStatus(httpStatus)
                .errorMessage(errorMessage)
                .retryCount(retryCount)
                .build());
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public Page<IntegrationLogResponse> getLogs(String integrationCode, Pageable pageable) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        return logRepository.findByTenantIdAndIntegrationCodeOrderByCreatedAtDesc(tenantId, integrationCode, pageable)
                .map(log -> IntegrationLogResponse.builder()
                        .date(DateFormatUtil.formatDisplay(log.getCreatedAt()))
                        .integration(log.getIntegrationCode())
                        .event(log.getEventName())
                        .action(log.getAction())
                        .status(log.getStatus())
                        .httpStatus(log.getHttpStatus())
                        .errorMessage(log.getErrorMessage())
                        .retryCount(log.getRetryCount())
                        .build());
    }
}
