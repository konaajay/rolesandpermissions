package com.project.www.repository;

import com.project.www.entity.TenantSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantSequenceRepository extends JpaRepository<TenantSequence, Long> {
    Optional<TenantSequence> findByTenantIdAndYear(Long tenantId, Integer year);
}
