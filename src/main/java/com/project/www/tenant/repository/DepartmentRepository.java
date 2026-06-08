package com.project.www.tenant.repository;

import com.project.www.tenant.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByTenantId(Long tenantId);
    List<Department> findByTenantIdAndActiveTrue(Long tenantId);
}
