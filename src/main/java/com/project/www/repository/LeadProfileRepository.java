package com.project.www.repository;

import com.project.www.entity.LeadProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadProfileRepository extends JpaRepository<LeadProfile, Long> {
    Optional<LeadProfile> findByUserIdAndTenantId(Long userId, Long tenantId);
}
