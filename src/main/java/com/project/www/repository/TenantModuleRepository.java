package com.project.www.repository;

import com.project.www.entity.TenantModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {
    List<TenantModule> findByTenantId(Long tenantId);
    List<TenantModule> findByTenantIdAndActiveTrue(Long tenantId);
    Optional<TenantModule> findByTenantIdAndModuleName(Long tenantId, String moduleName);
    boolean existsByTenantIdAndModuleNameAndActiveTrue(Long tenantId, String moduleName);
}
