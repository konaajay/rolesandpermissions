package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_key_usage_logs", indexes = {
        @Index(name = "idx_api_key_usage_tenant", columnList = "tenant_id"),
        @Index(name = "idx_api_key_usage_key", columnList = "api_key_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "api_key_id")
    private Long apiKeyId;

    @Column(length = 255)
    private String endpoint;

    @Column(length = 20)
    private String method;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
