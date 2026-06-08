package com.project.www.accessmanagement.repository;

import com.project.www.accessmanagement.entity.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {
    Optional<PlatformUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
