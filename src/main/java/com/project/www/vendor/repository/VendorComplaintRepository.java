package com.project.www.vendor.repository;

import com.project.www.vendor.entity.VendorComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorComplaintRepository extends JpaRepository<VendorComplaint, Long> {
    List<VendorComplaint> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(Long tenantId);
    Optional<VendorComplaint> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
    List<VendorComplaint> findByVendor_IdAndTenantIdAndDeletedFalse(Long vendorId, Long tenantId);
}
