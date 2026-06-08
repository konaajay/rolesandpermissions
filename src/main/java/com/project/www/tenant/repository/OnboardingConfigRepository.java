package com.project.www.tenant.repository;

import com.project.www.tenant.entity.OnboardingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingConfigRepository extends JpaRepository<OnboardingConfig, Long> {

    List<OnboardingConfig> findAllByTenantId(Long tenantId);

    Optional<OnboardingConfig> findByTenantIdAndRoleId(Long tenantId, Long roleId);
}
