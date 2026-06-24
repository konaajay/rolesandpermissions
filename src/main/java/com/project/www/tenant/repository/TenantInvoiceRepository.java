package com.project.www.tenant.repository;

import com.project.www.tenant.entity.TenantInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantInvoiceRepository extends JpaRepository<TenantInvoice, Long> {
    List<TenantInvoice> findByTenantId(Long tenantId);
}
