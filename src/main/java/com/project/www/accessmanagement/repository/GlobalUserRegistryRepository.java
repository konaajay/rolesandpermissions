package com.project.www.accessmanagement.repository;

import com.project.www.accessmanagement.entity.GlobalUserRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalUserRegistryRepository extends JpaRepository<GlobalUserRegistry, Long> {
    List<GlobalUserRegistry> findAllByEmail(String email);
    
    Optional<GlobalUserRegistry> findByEmail(String email);
    
    Optional<GlobalUserRegistry> findByEmailAndTenantCode(String email, String tenantCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndTenantCode(String email, String tenantCode);
    
    void deleteByEmailAndTenantCode(String email, String tenantCode);
    
    Optional<GlobalUserRegistry> findByTenantIdAndUserId(Long tenantId, Long userId);
}
