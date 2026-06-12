package com.project.www.tenant.repository;

import com.project.www.tenant.entity.WorkMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkModeRepository extends JpaRepository<WorkMode, Long> {
    List<WorkMode> findByTenantId(Long tenantId);
    List<WorkMode> findByTenantIdAndActiveTrue(Long tenantId);
    Optional<WorkMode> findByIdAndTenantId(Long id, Long tenantId);
}
