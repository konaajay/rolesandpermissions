package com.project.www.repository;

import com.project.www.entity.RoleHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {

    List<RoleHierarchy> findAllByRoleIdAndTenantId(Long roleId, Long tenantId);

    List<RoleHierarchy> findAllByRoleIdInAndTenantId(List<Long> roleIds, Long tenantId);

    Optional<RoleHierarchy> findByRoleIdAndReportsToRoleIdAndTenantId(Long roleId, Long reportsToRoleId, Long tenantId);
}
