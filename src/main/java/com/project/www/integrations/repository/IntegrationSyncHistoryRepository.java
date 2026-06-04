package com.project.www.integrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.IntegrationSyncHistory;

import java.util.List;

@Repository
public interface IntegrationSyncHistoryRepository extends JpaRepository<IntegrationSyncHistory, Long> {

    List<IntegrationSyncHistory> findByTenantIdAndTenantIntegrationIdOrderByStartedAtDesc(
            Long tenantId, Long tenantIntegrationId);

    List<IntegrationSyncHistory> findByTenantIntegrationIdOrderByStartedAtDesc(Long tenantIntegrationId);
}
