package com.project.www.repository;

import com.project.www.entity.TemplateDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateDefinitionRepository extends JpaRepository<TemplateDefinition, Long> {
    
    List<TemplateDefinition> findByTenantId(Long tenantId);
    
    List<TemplateDefinition> findByTenantIdAndTemplateType(Long tenantId, String templateType);

    Optional<TemplateDefinition> findByIdAndTenantId(Long id, Long tenantId);
    
    Optional<TemplateDefinition> findByTenantIdAndTemplateCode(Long tenantId, String templateCode);

    boolean existsByTenantIdAndTemplateCodeAndIdNot(Long tenantId, String templateCode, Long id);
}
