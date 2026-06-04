package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_integrations", indexes = {
        @Index(name = "idx_tenant_integration_tenant", columnList = "tenant_id"),
        @Index(name = "idx_tenant_integration_code", columnList = "tenant_id, code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "integration_definition_id", nullable = false)
    private Long integrationDefinitionId;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean connected = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private IntegrationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private IntegrationHealth health;

    @Column(length = 50)
    private String environment;

    @Column(name = "connected_by")
    private Long connectedBy;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_definition_id", insertable = false, updatable = false)
    private IntegrationDefinition integrationDefinition;
}
