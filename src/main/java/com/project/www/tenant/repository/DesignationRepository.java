package com.project.www.tenant.repository;

import com.project.www.tenant.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    List<Designation> findByTenantId(Long tenantId);
    List<Designation> findByTenantIdAndActiveTrue(Long tenantId);
    Optional<Designation> findByIdAndTenantId(Long id, Long tenantId);
}
