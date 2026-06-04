package com.project.www.integrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.IntegrationSetting;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationSettingRepository extends JpaRepository<IntegrationSetting, Long> {

    List<IntegrationSetting> findByTenantIntegrationId(Long tenantIntegrationId);

    Optional<IntegrationSetting> findByTenantIntegrationIdAndSettingKey(Long tenantIntegrationId, String settingKey);
}
