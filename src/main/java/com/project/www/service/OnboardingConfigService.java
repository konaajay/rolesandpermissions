package com.project.www.service;

import com.project.www.dto.OnboardingConfigDTO;
import com.project.www.entity.OnboardingConfig;
import com.project.www.entity.Role;
import com.project.www.entity.TemplateDefinition;
import com.project.www.repository.OnboardingConfigRepository;
import com.project.www.repository.RoleRepository;
import com.project.www.repository.TemplateDefinitionRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingConfigService {

    private final OnboardingConfigRepository repository;
    private final RoleRepository roleRepository;
    private final TemplateDefinitionRepository templateRepository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }

    public List<OnboardingConfigDTO> getAllConfigs() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findAllByTenantId(tenantId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public OnboardingConfigDTO getConfigByRoleId(Long roleId) {
        Long tenantId = TenantContext.getCurrentTenant();
        OnboardingConfig config = repository.findByTenantIdAndRoleId(tenantId, roleId)
                .orElse(null);
        if (config == null) return null;
        return mapToDto(config);
    }

    @Transactional
    public OnboardingConfigDTO saveOrUpdateConfig(Long roleId, OnboardingConfigDTO req) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!role.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied");
        }

        OnboardingConfig config = repository.findByTenantIdAndRoleId(tenantId, roleId)
                .orElse(new OnboardingConfig());

        if (config.getId() == null) {
            config.setTenantId(tenantId);
            config.setRole(role);
            config.setCreatedBy(username);
        } else {
            config.setUpdatedBy(username);
        }

        if (req.getAutoGenerateId() != null) config.setAutoGenerateId(req.getAutoGenerateId());
        if (req.getSendWelcomeEmail() != null) config.setSendWelcomeEmail(req.getSendWelcomeEmail());
        
        if (req.getGenerateDocument() != null) config.setGenerateDocument(req.getGenerateDocument());
        if (req.getDocumentTemplateId() != null) {
            TemplateDefinition template = templateRepository.findByIdAndTenantId(req.getDocumentTemplateId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Document template not found"));
            config.setDocumentTemplate(template);
        } else if (Boolean.FALSE.equals(req.getGenerateDocument())) {
            config.setDocumentTemplate(null);
        }

        if (req.getGenerateCertificate() != null) config.setGenerateCertificate(req.getGenerateCertificate());
        if (req.getCertificateTemplateId() != null) {
            TemplateDefinition certTemplate = templateRepository.findByIdAndTenantId(req.getCertificateTemplateId(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Certificate template not found"));
            config.setCertificateTemplate(certTemplate);
        } else if (Boolean.FALSE.equals(req.getGenerateCertificate())) {
            config.setCertificateTemplate(null);
        }

        if (req.getActive() != null) config.setActive(req.getActive());

        OnboardingConfig saved = repository.save(config);
        return mapToDto(saved);
    }

    private OnboardingConfigDTO mapToDto(OnboardingConfig entity) {
        OnboardingConfigDTO dto = new OnboardingConfigDTO();
        dto.setId(entity.getId());
        if (entity.getRole() != null) {
            dto.setRoleId(entity.getRole().getId());
            dto.setRoleName(entity.getRole().getName());
        }
        dto.setAutoGenerateId(entity.getAutoGenerateId());
        dto.setSendWelcomeEmail(entity.getSendWelcomeEmail());
        dto.setGenerateDocument(entity.getGenerateDocument());
        if (entity.getDocumentTemplate() != null) {
            dto.setDocumentTemplateId(entity.getDocumentTemplate().getId());
        }
        dto.setGenerateCertificate(entity.getGenerateCertificate());
        if (entity.getCertificateTemplate() != null) {
            dto.setCertificateTemplateId(entity.getCertificateTemplate().getId());
        }
        dto.setActive(entity.getActive());
        return dto;
    }
}
