package com.project.www.integrations.service;

public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        Long tenantId = TENANT_ID.get();
        return tenantId != null ? tenantId : 1L; // temporary default until auth module is merged
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        Long userId = USER_ID.get();
        return userId != null ? userId : 1L; // temporary default until auth module is merged
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
    }
}