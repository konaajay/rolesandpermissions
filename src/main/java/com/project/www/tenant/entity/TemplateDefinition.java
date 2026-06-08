package com.project.www.tenant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "template_definitions")
@Data
@NoArgsConstructor
public class TemplateDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType; // CERTIFICATE, DOCUMENT

    @Column(name = "template_code", nullable = false, length = 100)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Column(name = "background_image_url", length = 500)
    private String backgroundImageUrl;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_system_template", nullable = false)
    private Boolean isSystemTemplate = false;

    @Column(name = "is_editable", nullable = false)
    private Boolean isEditable = true;

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
