package com.project.www.repository;

import com.project.www.entity.VendorInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorInvoiceRepository extends JpaRepository<VendorInvoice, Long> {
    List<VendorInvoice> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(Long tenantId);
    Optional<VendorInvoice> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
}
