package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.project.www.integrations.enums.WebhookDeliveryStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_delivery_logs", indexes = {
        @Index(name = "idx_webhook_delivery_tenant", columnList = "tenant_id"),
        @Index(name = "idx_webhook_delivery_sub", columnList = "webhook_subscription_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "webhook_subscription_id")
    private Long webhookSubscriptionId;

    @Column(name = "event_name", length = 150)
    private String eventName;

    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    @Column(columnDefinition = "LONGTEXT")
    private String response;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private WebhookDeliveryStatus status;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
