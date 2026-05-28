package com.project.www.util;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantCode = new ThreadLocal<>();

    public static void setCurrentTenant(Long tenantId) {
        currentTenantId.set(tenantId);
    }

    public static Long getCurrentTenant() {
        return currentTenantId.get();
    }

    public static void setCurrentTenantCode(String tenantCode) {
        currentTenantCode.set(tenantCode);
    }

    public static String getCurrentTenantCode() {
        return currentTenantCode.get();
    }

    public static void clear() {
        currentTenantId.remove();
        currentTenantCode.remove();
    }
}
