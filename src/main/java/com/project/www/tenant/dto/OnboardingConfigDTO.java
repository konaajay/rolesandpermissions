package com.project.www.tenant.dto;

import lombok.Data;

@Data
public class OnboardingConfigDTO {
    private Long id;
    private Long roleId;
    private String roleName;
    private Boolean autoGenerateId;
    private Boolean sendWelcomeEmail;
    private Boolean generateDocument;
    private Long documentTemplateId;
    private Boolean generateCertificate;
    private Long certificateTemplateId;
    private Boolean active;
}
