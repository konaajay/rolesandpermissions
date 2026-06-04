package com.project.www.integrations.service;

import java.util.Map;
import java.util.Optional;

public interface IntegrationSettingService {

    void saveSetting(Long tenantIntegrationId, String key, String value, boolean encrypted);

    void saveSettings(Long tenantIntegrationId, Map<String, String> settings);

    Optional<String> getSetting(Long tenantIntegrationId, String key);

    /**
     * Convenience wrapper that returns the setting value as a plain String (or null).
     * It trims the value to avoid hidden whitespace issues.
     */
    default String getSettingValue(Long tenantIntegrationId, String key) {
        return getSetting(tenantIntegrationId, key)
                .map(String::trim)
                .orElse(null);
    }

    String getSettingOrDefault(Long tenantIntegrationId, String key, String defaultValue);

    Map<String, String> getAllSettings(Long tenantIntegrationId);
}
