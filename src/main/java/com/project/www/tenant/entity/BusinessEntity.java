package com.project.www.tenant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_entities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "entity_code", nullable = false, length = 100)
    private String entityCode;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "show_in_user_form", nullable = false)
    @Builder.Default
    private Boolean showInUserForm = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
