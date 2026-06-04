package com.project.www.integrations.config;

/**
 * Future permission constants for integration module.
 * TODO: Enforce via Spring Security when JWT/login is enabled.
 */
public final class IntegrationPermissions {

    public static final String INTEGRATION_VIEW = "INTEGRATION_VIEW";
    public static final String INTEGRATION_CREATE = "INTEGRATION_CREATE";
    public static final String INTEGRATION_UPDATE = "INTEGRATION_UPDATE";
    public static final String INTEGRATION_DELETE = "INTEGRATION_DELETE";
    public static final String INTEGRATION_CONNECT = "INTEGRATION_CONNECT";
    public static final String INTEGRATION_DISCONNECT = "INTEGRATION_DISCONNECT";
    public static final String INTEGRATION_TEST = "INTEGRATION_TEST";
    public static final String INTEGRATION_LOG_VIEW = "INTEGRATION_LOG_VIEW";
    public static final String INTEGRATION_API_KEY_MANAGE = "INTEGRATION_API_KEY_MANAGE";
    public static final String INTEGRATION_WEBHOOK_MANAGE = "INTEGRATION_WEBHOOK_MANAGE";
    public static final String PAYMENT_CONFIGURE = "PAYMENT_CONFIGURE";
    public static final String PAYMENT_VIEW_LOGS = "PAYMENT_VIEW_LOGS";

    private IntegrationPermissions() {
    }
}
