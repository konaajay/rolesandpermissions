package com.project.www.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver {

    public String resolveTenantCode(HttpServletRequest request) {
        // 1. Check HTTP header "X-Tenant"
        String tenantHeader = request.getHeader("X-Tenant");
        if (tenantHeader != null && !tenantHeader.trim().isEmpty()) {
            return tenantHeader.trim().toUpperCase();
        }

        // 2. Check subdomain (e.g. google.localhost or goog.domain.com)
        String serverName = request.getServerName();
        if (serverName != null) {
            String[] parts = serverName.split("\\.");
            if (parts.length > 2) {
                String subdomain = parts[0].toUpperCase();
                // Ignore common standard subdomains
                if (!subdomain.equals("WWW") && !subdomain.equals("API") && !subdomain.equals("APP")) {
                    return subdomain;
                }
            }
        }

        return null;
    }
}
