package com.project.www.tenant.repository;

import com.project.www.tenant.entity.OfficeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, Long> {
    java.util.List<OfficeLocation> findAllByTenantId(Long tenantId);
    java.util.Optional<OfficeLocation> findByIdAndTenantId(Long id, Long tenantId);
}

