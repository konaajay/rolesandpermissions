package com.project.www.integrations.service;

import com.project.www.integrations.entity.IntegrationDefinition;
import com.project.www.integrations.entity.TenantIntegration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantIntegrationContext {
    private Long tenantId;
    private IntegrationDefinition definition;
    private TenantIntegration tenantIntegration;
}
