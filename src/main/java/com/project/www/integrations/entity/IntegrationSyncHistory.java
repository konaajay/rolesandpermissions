package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "integration_sync_history", indexes = {
        @Index(name = "idx_sync_history_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sync_history_tenant_integration", columnList = "tenant_integration_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationSyncHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_integration_id", nullable = false)
    private Long tenantIntegrationId;

    @Column(name = "sync_type", length = 100)
    private String syncType;

    @Column(length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "records_success")
    private Integer recordsSuccess;

    @Column(name = "records_failed")
    private Integer recordsFailed;
}
