package com.project.www.invoice.repository;

import com.project.www.invoice.entity.InvoiceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceConfigurationRepository extends JpaRepository<InvoiceConfiguration, Long> {

    List<InvoiceConfiguration> findByTenantIdAndDeletedFalse(Long tenantId);

    Optional<InvoiceConfiguration> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    Optional<InvoiceConfiguration> findByTenantIdAndTargetModuleAndActiveTrueAndDeletedFalse(Long tenantId, String targetModule);

    Optional<InvoiceConfiguration> findByTenantIdAndActiveTrueAndDeletedFalse(Long tenantId);

    @Modifying
    @Query("UPDATE InvoiceConfiguration i SET i.active = false WHERE i.tenantId = :tenantId AND i.targetModule = :targetModule AND i.active = true")
    void deactivateAllForTenantAndModule(@Param("tenantId") Long tenantId, @Param("targetModule") String targetModule);
}
