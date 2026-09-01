package com.project.www.vendor.repository;

import com.project.www.vendor.entity.ReceivedProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ReceivedProductRepository extends JpaRepository<ReceivedProduct, Long> {
    @Query(value = "SELECT r FROM ReceivedProduct r LEFT JOIN FETCH r.requirementItem ri WHERE (:tenantId IS NULL OR r.tenantId = :tenantId) AND (r.deleted = false OR r.deleted IS NULL)",
           countQuery = "SELECT count(r) FROM ReceivedProduct r WHERE (:tenantId IS NULL OR r.tenantId = :tenantId) AND (r.deleted = false OR r.deleted IS NULL)")
    Page<ReceivedProduct> findByTenantIdAndDeletedFalse(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT r FROM ReceivedProduct r LEFT JOIN FETCH r.requirementItem ri WHERE r.id = :id AND (:tenantId IS NULL OR r.tenantId = :tenantId) AND (r.deleted = false OR r.deleted IS NULL)")
    Optional<ReceivedProduct> findByIdAndTenantIdAndDeletedFalse(@Param("id") Long id, @Param("tenantId") Long tenantId);
    
    Optional<ReceivedProduct> findByRequirementItemIdAndTenantIdAndDeletedFalse(Long requirementItemId, Long tenantId);
    
    List<ReceivedProduct> findByRequirementItem_Requirement_IdAndTenantIdAndDeletedFalse(Long requirementId, Long tenantId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReceivedProduct r WHERE r.id = :id AND (:tenantId IS NULL OR r.tenantId = :tenantId) AND (r.deleted = false OR r.deleted IS NULL)")
    Optional<ReceivedProduct> findByIdForUpdate(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
