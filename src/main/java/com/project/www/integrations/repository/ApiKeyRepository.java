package com.project.www.integrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.ApiKey;
import com.project.www.integrations.enums.ApiKeyStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByTenantId(Long tenantId);

    Optional<ApiKey> findByTenantIdAndId(Long tenantId, Long id);

    Optional<ApiKey> findByApiKeyHashAndStatus(String apiKeyHash, ApiKeyStatus status);
}
