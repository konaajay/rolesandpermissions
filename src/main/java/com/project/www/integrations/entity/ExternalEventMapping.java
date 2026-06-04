package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_event_mappings", indexes = {
        @Index(name = "idx_external_event_tenant", columnList = "tenant_id"),
        @Index(name = "idx_external_event_provider", columnList = "tenant_id, provider, external_event_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEventMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(length = 100)
    private String provider;

    @Column(name = "external_event_id", length = 255)
    private String externalEventId;

    @Column(name = "internal_module", length = 100)
    private String internalModule;

    @Column(name = "internal_reference_id")
    private Long internalReferenceId;

    @Column(name = "metadata_json", columnDefinition = "LONGTEXT")
    private String metadataJson;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
