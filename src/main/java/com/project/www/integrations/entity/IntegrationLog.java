package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "integration_logs", indexes = {
        @Index(name = "idx_integration_log_tenant", columnList = "tenant_id"),
        @Index(name = "idx_integration_log_code", columnList = "tenant_id, integration_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_integration_id")
    private Long tenantIntegrationId;

    @Column(name = "integration_code", length = 100)
    private String integrationCode;

    @Column(name = "event_name", length = 150)
    private String eventName;

    @Column(length = 150)
    private String action;

    @Column(name = "request_payload", columnDefinition = "LONGTEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "LONGTEXT")
    private String responsePayload;

    @Column(length = 50)
    private String status;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
