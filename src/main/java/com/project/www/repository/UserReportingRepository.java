package com.project.www.repository;

import com.project.www.entity.UserReporting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReportingRepository extends JpaRepository<UserReporting, Long> {

    List<UserReporting> findAllByUserIdAndTenantId(Long userId, Long tenantId);

    List<UserReporting> findAllBySupervisorUserIdAndTenantId(Long supervisorUserId, Long tenantId);

    Optional<UserReporting> findByUserIdAndSupervisorUserIdAndTenantId(Long userId, Long supervisorUserId, Long tenantId);

    void deleteAllByUserIdAndTenantId(Long userId, Long tenantId);
}
