package com.project.www.repository;

import com.project.www.entity.UserExtraFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExtraFieldValueRepository extends JpaRepository<UserExtraFieldValue, Long> {

    List<UserExtraFieldValue> findAllByUserIdAndTenantId(Long userId, Long tenantId);

    List<UserExtraFieldValue> findAllByUserIdInAndTenantId(List<Long> userIds, Long tenantId);

    Optional<UserExtraFieldValue> findByUserIdAndFieldIdAndTenantId(Long userId, Long fieldId, Long tenantId);

    void deleteAllByUserIdAndTenantId(Long userId, Long tenantId);
}
