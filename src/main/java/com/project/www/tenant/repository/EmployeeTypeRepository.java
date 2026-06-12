package com.project.www.tenant.repository;

import com.project.www.tenant.entity.EmployeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTypeRepository extends JpaRepository<EmployeeType, Long> {
    List<EmployeeType> findByTenantId(Long tenantId);
    List<EmployeeType> findByTenantIdAndActiveTrue(Long tenantId);
    Optional<EmployeeType> findByIdAndTenantId(Long id, Long tenantId);
}
