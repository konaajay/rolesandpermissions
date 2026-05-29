package com.project.www.repository;

import com.project.www.entity.IdFormatSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdFormatSettingRepository extends JpaRepository<IdFormatSetting, Long> {
    List<IdFormatSetting> findByTenantId(Long tenantId);
    Optional<IdFormatSetting> findByTenantIdAndEntityType(Long tenantId, String entityType);
}
