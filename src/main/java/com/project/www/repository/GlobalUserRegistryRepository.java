package com.project.www.repository;

import com.project.www.entity.GlobalUserRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalUserRegistryRepository extends JpaRepository<GlobalUserRegistry, Long> {
    Optional<GlobalUserRegistry> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    void deleteByEmail(String email);
    
    Optional<GlobalUserRegistry> findByTenantIdAndUserId(Long tenantId, Long userId);
}
