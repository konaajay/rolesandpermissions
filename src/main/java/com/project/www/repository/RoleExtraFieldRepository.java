package com.project.www.repository;

import com.project.www.entity.RoleExtraField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleExtraFieldRepository extends JpaRepository<RoleExtraField, Long> {

    List<RoleExtraField> findAllByRoleIdAndTenantIdAndActiveTrueOrderByDisplayOrderAsc(Long roleId, Long tenantId);

    List<RoleExtraField> findAllByRoleIdInAndTenantIdAndActiveTrueOrderByDisplayOrderAsc(List<Long> roleIds, Long tenantId);

    List<RoleExtraField> findAllByTenantId(Long tenantId);

    Optional<RoleExtraField> findByRoleIdAndFieldNameAndTenantId(Long roleId, String fieldName, Long tenantId);
}
