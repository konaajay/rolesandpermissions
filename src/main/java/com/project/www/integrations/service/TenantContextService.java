package com.project.www.integrations.service;

import org.springframework.stereotype.Service;

/**
 * Provides current tenant context for multi-tenant operations.
 * TEMPORARY SUPER ADMIN MODE: returns default tenant 1.
 * TODO: Replace with JWT/session tenant resolution when auth is enabled.
 */
@Service
public class TenantContextService {

    public Long getCurrentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId != null ? tenantId : 1L;
    }

    public Long getCurrentUserId() {
        return TenantContextHolder.getUserId();
    }
}
