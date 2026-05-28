package com.project.www.repository;

import com.project.www.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNameAndTenantId(String name, Long tenantId);

    Optional<Role> findByCodeAndTenantId(String code, Long tenantId);
    
    boolean existsByNameAndTenantId(String name, Long tenantId);

    boolean existsByCodeAndTenantId(String code, Long tenantId);

    Optional<Role> findByIdAndTenantId(Long id, Long tenantId);

    List<Role> findAllByTenantId(Long tenantId);
}