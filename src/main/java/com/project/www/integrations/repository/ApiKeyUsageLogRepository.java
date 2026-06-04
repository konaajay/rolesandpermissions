package com.project.www.integrations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.ApiKeyUsageLog;

@Repository
public interface ApiKeyUsageLogRepository extends JpaRepository<ApiKeyUsageLog, Long> {

    Page<ApiKeyUsageLog> findByTenantIdAndApiKeyIdOrderByCreatedAtDesc(
            Long tenantId, Long apiKeyId, Pageable pageable);
}
