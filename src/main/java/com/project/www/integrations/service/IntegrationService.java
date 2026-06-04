package com.project.www.integrations.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.www.integrations.dto.*;

import java.util.List;

public interface IntegrationService {

    List<IntegrationCardResponse> getAllIntegrations();

    IntegrationDetailsResponse getIntegrationDetails(String code);

    IntegrationDetailsResponse toggleIntegration(String code, IntegrationToggleRequest request);

    IntegrationDetailsResponse configureIntegration(String code, IntegrationConfigureRequest request);

    IntegrationTestResponse testIntegration(String code);

    void disconnectIntegration(String code);

    Page<IntegrationLogResponse> getIntegrationLogs(String code, Pageable pageable);

    List<SyncHistoryResponse> getSyncHistory(String code);

    OAuthConnectResponse getOAuthConnectUrl(String code);

    String handleOAuthCallback(String code, String authCode, String state);

    TenantIntegrationContext resolveContext(String code);
}
