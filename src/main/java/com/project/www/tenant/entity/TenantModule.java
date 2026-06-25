package com.project.www.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_modules", uniqueConstraints = {
        @UniqueConstraint(name = "UNIQUE_tenant_module", columnNames = {"tenant_id", "module_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "module_name", nullable = false)
    private String moduleName;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "amount")
    private Double amount;



    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "extra_charges")
    private Double extraCharges;

    @Column(name = "start_date")
    private java.time.LocalDate startDate;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
