package com.project.www.repository;

import com.project.www.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionKeyAndTenantId(String permissionKey, Long tenantId);
    
    boolean existsByPermissionKeyAndTenantId(String permissionKey, Long tenantId);

    Optional<Permission> findByIdAndTenantId(Long id, Long tenantId);

    List<Permission> findAllByTenantId(Long tenantId);
}