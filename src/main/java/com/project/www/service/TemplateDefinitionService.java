package com.project.www.service;

import com.project.www.entity.TemplateDefinition;
import com.project.www.repository.TemplateDefinitionRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateDefinitionService {

    private final TemplateDefinitionRepository repository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }

    public List<TemplateDefinition> getAllTemplates(String type) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (type != null && !type.isBlank()) {
            return repository.findByTenantIdAndTemplateType(tenantId, type.toUpperCase());
        }
        return repository.findByTenantId(tenantId);
    }

    public TemplateDefinition getTemplateById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    @Transactional
    public TemplateDefinition createTemplate(TemplateDefinition template) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();

        if (template.getTemplateCode() == null || template.getTemplateCode().isBlank()) {
            throw new RuntimeException("Template code is required");
        }

        template.setTemplateCode(template.getTemplateCode().toUpperCase().replaceAll("\\s+", "_"));

        if (repository.findByTenantIdAndTemplateCode(tenantId, template.getTemplateCode()).isPresent()) {
            throw new RuntimeException("A template with this code already exists.");
        }

        template.setTenantId(tenantId);
        template.setCreatedBy(username);
        template.setTemplateType(template.getTemplateType().toUpperCase());
        if (template.getActive() == null) template.setActive(true);

        return repository.save(template);
    }

    @Transactional
    public TemplateDefinition updateTemplate(Long id, TemplateDefinition req) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();

        TemplateDefinition existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        if (Boolean.TRUE.equals(existing.getIsSystemTemplate()) && Boolean.FALSE.equals(existing.getIsEditable())) {
            throw new RuntimeException("System templates cannot be edited. Please clone it to create a custom template.");
        }

        if (req.getTemplateCode() != null) {
            String newCode = req.getTemplateCode().toUpperCase().replaceAll("\\s+", "_");
            if (repository.existsByTenantIdAndTemplateCodeAndIdNot(tenantId, newCode, id)) {
                throw new RuntimeException("A template with this code already exists.");
            }
            existing.setTemplateCode(newCode);
        }

        if (req.getTemplateName() != null) existing.setTemplateName(req.getTemplateName());
        if (req.getTemplateType() != null) existing.setTemplateType(req.getTemplateType().toUpperCase());
        if (req.getContentHtml() != null) existing.setContentHtml(req.getContentHtml());
        if (req.getBackgroundImageUrl() != null) existing.setBackgroundImageUrl(req.getBackgroundImageUrl());
        if (req.getActive() != null) existing.setActive(req.getActive());

        existing.setUpdatedBy(username);

        return repository.save(existing);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        TemplateDefinition existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
                
        if (Boolean.TRUE.equals(existing.getIsSystemTemplate())) {
            throw new RuntimeException("System templates cannot be deleted.");
        }
        
        repository.delete(existing);
    }

    private static TemplateDefinition createSystemTemplate(String code, String name, String type, String html) {
        TemplateDefinition t = new TemplateDefinition();
        t.setTemplateCode(code);
        t.setTemplateName(name);
        t.setTemplateType(type);
        t.setContentHtml(html);
        t.setIsSystemTemplate(true);
        t.setIsEditable(false);
        return t;
    }

    private static final List<TemplateDefinition> SYSTEM_TEMPLATES = List.of(
            createSystemTemplate("OFFER_LETTER", "Offer Letter", "DOCUMENT", "<h1>Offer Letter</h1>\n\n<p>Dear {{FIRST_NAME}} {{LAST_NAME}},</p>\n\n<p>We are pleased to offer you the position of <b>{{ROLE}}</b> at {{TENANT_NAME}}.</p>\n\n<p>Employee ID: {{EMPLOYEE_ID}}</p>\n<p>Joining Date: {{CURRENT_DATE}}</p>"),
            createSystemTemplate("APPOINTMENT_LETTER", "Appointment Letter", "DOCUMENT", "<h1>Appointment Letter</h1>\n<p>Dear {{FIRST_NAME}}, welcome aboard!</p>"),
            createSystemTemplate("JOINING_LETTER", "Joining Letter", "DOCUMENT", "<h1>Joining Letter</h1>\n<p>This confirms the joining of {{FIRST_NAME}} {{LAST_NAME}}.</p>"),
            createSystemTemplate("EXPERIENCE_LETTER", "Experience Letter", "DOCUMENT", "<h1>Experience Letter</h1>\n<p>This is to certify that {{FIRST_NAME}} worked as {{ROLE}}.</p>"),
            createSystemTemplate("RELIEVING_LETTER", "Relieving Letter", "DOCUMENT", "<h1>Relieving Letter</h1>\n<p>{{FIRST_NAME}} has been relieved from their duties.</p>"),
            createSystemTemplate("WARNING_LETTER", "Warning Letter", "DOCUMENT", "<h1>Warning Letter</h1>\n<p>Official warning for {{FIRST_NAME}}.</p>"),
            createSystemTemplate("PROMOTION_LETTER", "Promotion Letter", "DOCUMENT", "<h1>Promotion Letter</h1>\n<p>Congratulations {{FIRST_NAME}} on your promotion.</p>"),
            createSystemTemplate("TRANSFER_LETTER", "Transfer Letter", "DOCUMENT", "<h1>Transfer Letter</h1>\n<p>{{FIRST_NAME}} is transferred to {{DEPARTMENT}}.</p>"),
            createSystemTemplate("INTERNSHIP_CERTIFICATE", "Internship Certificate", "CERTIFICATE", "<h1>Internship Certificate</h1>\n<p>Awarded to {{FIRST_NAME}}.</p>"),
            createSystemTemplate("COURSE_COMPLETION_CERTIFICATE", "Course Completion Certificate", "CERTIFICATE", "<h1>Course Completion Certificate</h1>\n<p>Awarded to {{FIRST_NAME}}.</p>"),
            createSystemTemplate("TRAINING_CERTIFICATE", "Training Certificate", "CERTIFICATE", "<h1>Training Certificate</h1>\n<p>Awarded to {{FIRST_NAME}}.</p>"),
            createSystemTemplate("ACHIEVEMENT_CERTIFICATE", "Achievement Certificate", "CERTIFICATE", "<h1>Achievement Certificate</h1>\n<p>Awarded to {{FIRST_NAME}}.</p>"),
            createSystemTemplate("PARTICIPATION_CERTIFICATE", "Participation Certificate", "CERTIFICATE", "<h1>Participation Certificate</h1>\n<p>Awarded to {{FIRST_NAME}}.</p>"),
            createSystemTemplate("EMPLOYEE_RECOGNITION_CERTIFICATE", "Employee Recognition Certificate", "CERTIFICATE", "<h1>Employee Recognition Certificate</h1>\n<p>Awarded to {{FIRST_NAME}}.</p>")
    );

    public List<TemplateDefinition> getAvailableSystemTemplates() {
        return SYSTEM_TEMPLATES;
    }

    @Transactional
    public List<TemplateDefinition> importSystemTemplates(List<String> templateCodes) {
        Long tenantId = TenantContext.getCurrentTenant();
        String username = getCurrentUsername();
        
        List<TemplateDefinition> imported = new java.util.ArrayList<>();
        
        for (String code : templateCodes) {
            if (repository.findByTenantIdAndTemplateCode(tenantId, code).isEmpty()) {
                SYSTEM_TEMPLATES.stream()
                        .filter(t -> t.getTemplateCode().equals(code))
                        .findFirst()
                        .ifPresent(sys -> {
                            TemplateDefinition newTemp = new TemplateDefinition();
                            newTemp.setTenantId(tenantId);
                            newTemp.setTemplateCode(sys.getTemplateCode());
                            newTemp.setTemplateName(sys.getTemplateName());
                            newTemp.setTemplateType(sys.getTemplateType());
                            newTemp.setContentHtml(sys.getContentHtml());
                            newTemp.setIsSystemTemplate(sys.getIsSystemTemplate());
                            newTemp.setIsEditable(sys.getIsEditable());
                            newTemp.setCreatedBy(username);
                            imported.add(repository.save(newTemp));
                        });
            }
        }
        return imported;
    }
}
