package com.project.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id"),
        @Index(name = "idx_order_campaign", columnList = "campaign_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(name = "campaign_id_snapshot")
    private Long campaignIdSnapshot; // Immutable snapshot for attribution history

    // UTM Attribution
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal walletUsed = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    private String couponCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal finalPaidAmount;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING, PAID, REFUNDED, FAILED

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
