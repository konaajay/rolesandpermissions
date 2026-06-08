package com.project.www.tenant.repository;

import com.project.www.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    boolean existsByName(String name);
    Optional<Tenant> findByCode(String code);
    Optional<Tenant> findByDomain(String domain);
    boolean existsByCode(String code);
}
