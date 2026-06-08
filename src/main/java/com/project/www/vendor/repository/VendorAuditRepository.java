package com.project.www.vendor.repository;

import com.project.www.vendor.entity.VendorAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorAuditRepository extends JpaRepository<VendorAudit, Long> {
    List<VendorAudit> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(Long tenantId);
    Optional<VendorAudit> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
}
