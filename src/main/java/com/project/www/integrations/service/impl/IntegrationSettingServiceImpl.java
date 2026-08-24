package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.entity.IntegrationSetting;
import com.project.www.integrations.repository.IntegrationSettingRepository;
import com.project.www.integrations.service.EncryptionService;
import com.project.www.integrations.service.IntegrationSettingService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegrationSettingServiceImpl implements IntegrationSettingService {

    private final IntegrationSettingRepository settingRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional("integrationTransactionManager")
    public void saveSetting(Long tenantIntegrationId, String key, String value, boolean encrypted) {
        if (key == null || value == null) {
            return;
        }
        String storedValue = encrypted ? encryptionService.encrypt(value) : value;
        IntegrationSetting setting = settingRepository
                .findByTenantIntegrationIdAndSettingKey(tenantIntegrationId, key)
                .orElse(IntegrationSetting.builder()
                        .tenantIntegrationId(tenantIntegrationId)
                        .settingKey(key)
                        .build());
        setting.setSettingValue(storedValue);
        setting.setEncrypted(encrypted);
        settingRepository.save(setting);
    }

    @Override
    @Transactional("integrationTransactionManager")
    public void saveSettings(Long tenantIntegrationId, Map<String, String> settings) {
        if (settings == null) {
            return;
        }
        settings.forEach((k, v) -> {
            String dbKey = camelToSnake(k);
            boolean enc = dbKey.contains("token") || dbKey.contains("secret");
            saveSetting(tenantIntegrationId, dbKey, v, enc);
        });
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public Optional<String> getSetting(Long tenantIntegrationId, String key) {
        return settingRepository.findByTenantIntegrationIdAndSettingKey(tenantIntegrationId, key)
                .map(s -> s.getEncrypted() ? encryptionService.decrypt(s.getSettingValue()) : s.getSettingValue());
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public String getSettingOrDefault(Long tenantIntegrationId, String key, String defaultValue) {
        return getSetting(tenantIntegrationId, key).orElse(defaultValue);
    }

    @Override
    @Transactional(value = "integrationTransactionManager", readOnly = true)
    public Map<String, String> getAllSettings(Long tenantIntegrationId) {
        return settingRepository.findByTenantIntegrationId(tenantIntegrationId).stream()
                .collect(Collectors.toMap(
                        IntegrationSetting::getSettingKey,
                        s -> s.getEncrypted() ? encryptionService.decrypt(s.getSettingValue()) : s.getSettingValue(),
                        (a, b) -> b,
                        HashMap::new
                ));
    }

    private String camelToSnake(String input) {
        if (input == null) return null;
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
