package com.project.www.vendor.repository;

import com.project.www.vendor.entity.VendorContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorContractRepository extends JpaRepository<VendorContract, Long> {
    List<VendorContract> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(Long tenantId);
    Optional<VendorContract> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
}
