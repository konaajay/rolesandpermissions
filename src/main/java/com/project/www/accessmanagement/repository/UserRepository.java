package com.project.www.accessmanagement.repository;

import com.project.www.accessmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    List<User> findAllByTenantId(Long tenantId);

    Optional<User> findByEmail(String email);


    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName AND u.tenantId = :tenantId AND u.active = true")
    long countByRoleNameAndTenantIdAndActiveTrue(@org.springframework.data.repository.query.Param("roleName") String roleName, @org.springframework.data.repository.query.Param("tenantId") Long tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT u.id FROM User u WHERE u.id = ?1") // Mock implementation to compile
    List<Long> findSubordinateIds(Long id);

    @org.springframework.data.jpa.repository.Query("SELECT u.id FROM User u")
    List<Long> findAllIds();
}