package com.project.www.vendor.repository;

import com.project.www.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    Page<Vendor> findByTenantIdAndDeletedFalse(Long tenantId, Pageable pageable);

    @Query("SELECT v FROM Vendor v WHERE v.tenantId = :tenantId AND v.deleted = false AND " +
           "(LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.vendorCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.mobileNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Vendor> searchVendors(@Param("tenantId") Long tenantId, @Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByTenantIdAndVendorCodeAndDeletedFalse(Long tenantId, String vendorCode);
    boolean existsByTenantIdAndEmailAndDeletedFalse(Long tenantId, String email);
    boolean existsByTenantIdAndMobileNumberAndDeletedFalse(Long tenantId, String mobileNumber);
}
