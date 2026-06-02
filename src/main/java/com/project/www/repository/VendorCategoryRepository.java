package com.project.www.repository;

import com.project.www.entity.VendorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorCategoryRepository extends JpaRepository<VendorCategory, Long> {
    List<VendorCategory> findByTenantIdAndDeletedFalse(Long tenantId);
    Optional<VendorCategory> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);
    boolean existsByTenantIdAndNameAndDeletedFalse(Long tenantId, String name);
}
