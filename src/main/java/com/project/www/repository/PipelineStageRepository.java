package com.project.www.repository;

import com.project.www.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {
    List<PipelineStage> findAllByTenantIdOrderByOrderIndexAsc(Long tenantId);
    List<PipelineStage> findByTenantIdAndActiveTrueOrderByOrderIndexAsc(Long tenantId);
    Optional<PipelineStage> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndStatusValue(Long tenantId, String statusValue);
}
