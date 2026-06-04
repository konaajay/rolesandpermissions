package com.project.www.integrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.TenantIntegration;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantIntegrationRepository extends JpaRepository<TenantIntegration, Long> {

    List<TenantIntegration> findByTenantId(Long tenantId);

    Optional<TenantIntegration> findByTenantIdAndCode(Long tenantId, String code);

    List<TenantIntegration> findByTenantIdAndEnabledTrue(Long tenantId);

    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
