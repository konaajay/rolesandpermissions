package com.project.www.entity;

import com.project.www.enums.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon_usage", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "coupon_id", "learner_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(nullable = false)
    private int usageCount = 0;

    private Long orderId;

    @Column(nullable = false)
    private java.time.LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        usedAt = java.time.LocalDateTime.now();
    }
}
