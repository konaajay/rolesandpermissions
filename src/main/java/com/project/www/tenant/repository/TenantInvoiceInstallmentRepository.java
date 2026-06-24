package com.project.www.tenant.repository;

import com.project.www.tenant.entity.TenantInvoiceInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantInvoiceInstallmentRepository extends JpaRepository<TenantInvoiceInstallment, Long> {
    List<TenantInvoiceInstallment> findByInvoiceId(Long invoiceId);
}
