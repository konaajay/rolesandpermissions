package com.project.www.vendor.repository;

import com.project.www.vendor.entity.ProductLifecycleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductLifecycleEventRepository extends JpaRepository<ProductLifecycleEvent, Long> {
    List<ProductLifecycleEvent> findByAssignmentIdAndTenantIdOrderByCreatedAtDesc(Long assignmentId, Long tenantId);
}
