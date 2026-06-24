package com.project.www.tenant.repository;

import com.project.www.tenant.entity.TenantInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantInvoiceItemRepository extends JpaRepository<TenantInvoiceItem, Long> {
    List<TenantInvoiceItem> findByInvoiceId(Long invoiceId);
}
