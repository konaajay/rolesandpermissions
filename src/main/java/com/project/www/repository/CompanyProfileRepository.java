package com.project.www.repository;

import com.project.www.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByTenantId(Long tenantId);
    boolean existsByTenantIdAndGstNumberAndIdNot(Long tenantId, String gstNumber, Long id);
    boolean existsByTenantIdAndGstNumber(Long tenantId, String gstNumber);
}
