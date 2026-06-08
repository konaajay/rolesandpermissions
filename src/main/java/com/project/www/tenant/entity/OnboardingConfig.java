package com.project.www.tenant.entity;

import com.project.www.accessmanagement.entity.Role;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_configs")
@Data
@NoArgsConstructor
public class OnboardingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "auto_generate_id", nullable = false)
    private Boolean autoGenerateId = true;

    @Column(name = "send_welcome_email", nullable = false)
    private Boolean sendWelcomeEmail = true;

    @Column(name = "generate_document", nullable = false)
    private Boolean generateDocument = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_template_id")
    private TemplateDefinition documentTemplate;

    @Column(name = "generate_certificate", nullable = false)
    private Boolean generateCertificate = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "certificate_template_id")
    private TemplateDefinition certificateTemplate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
