package com.project.www.tenant.repository;

import com.project.www.tenant.entity.BusinessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessEntityRepository extends JpaRepository<BusinessEntity, Long> {
    List<BusinessEntity> findByTenantId(Long tenantId);
    List<BusinessEntity> findByTenantIdAndActiveTrue(Long tenantId);
}
