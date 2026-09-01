package com.project.www.vendor.repository;

import com.project.www.vendor.entity.ProductAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAssignmentRepository extends JpaRepository<ProductAssignment, Long> {
    @Query(value = "SELECT p FROM ProductAssignment p LEFT JOIN FETCH p.receivedProduct rp LEFT JOIN FETCH rp.requirementItem ri LEFT JOIN FETCH p.assignedUser u WHERE (:tenantId IS NULL OR p.tenantId = :tenantId) AND (p.deleted = false OR p.deleted IS NULL)",
           countQuery = "SELECT count(p) FROM ProductAssignment p WHERE (:tenantId IS NULL OR p.tenantId = :tenantId) AND (p.deleted = false OR p.deleted IS NULL)")
    Page<ProductAssignment> findByTenantIdAndDeletedFalse(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query(value = "SELECT p FROM ProductAssignment p LEFT JOIN FETCH p.receivedProduct rp LEFT JOIN FETCH rp.requirementItem ri LEFT JOIN FETCH p.assignedUser u WHERE (:tenantId IS NULL OR p.tenantId = :tenantId) AND rp.id = :receivedProductId AND (p.deleted = false OR p.deleted IS NULL)",
           countQuery = "SELECT count(p) FROM ProductAssignment p WHERE (:tenantId IS NULL OR p.tenantId = :tenantId) AND p.receivedProduct.id = :receivedProductId AND (p.deleted = false OR p.deleted IS NULL)")
    Page<ProductAssignment> findByTenantIdAndReceivedProductIdAndDeletedFalse(@Param("tenantId") Long tenantId, @Param("receivedProductId") Long receivedProductId, Pageable pageable);

    @Query("SELECT p FROM ProductAssignment p LEFT JOIN FETCH p.receivedProduct rp LEFT JOIN FETCH rp.requirementItem ri LEFT JOIN FETCH p.assignedUser u WHERE (:tenantId IS NULL OR p.tenantId = :tenantId) AND rp.id = :receivedProductId AND (p.deleted = false OR p.deleted IS NULL)")
    List<ProductAssignment> findByTenantIdAndReceivedProductIdAndDeletedFalse(@Param("tenantId") Long tenantId, @Param("receivedProductId") Long receivedProductId);
}
