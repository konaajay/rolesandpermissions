package com.project.www.integrations.service;

import com.project.www.util.TenantContext;
import com.project.www.security.UserContext;
import org.springframework.stereotype.Service;

/**
 * Provides current tenant context for multi-tenant operations.
 * Uses the main application's JWT-based tenant resolution.
 */
@Service
public class TenantContextService {

    public Long getCurrentTenantId() {
        return TenantContext.getCurrentTenant();
    }

    public Long getCurrentUserId() {
        return UserContext.getCurrentUserId();
    }
}
