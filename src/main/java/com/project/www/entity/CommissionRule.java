package com.project.www.entity;

import java.math.BigDecimal;
import com.project.www.enums.*;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "commission_rules", indexes = {
        @Index(name = "idx_course_rule", columnList = "courseId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(name = "commission_percent", nullable = false, precision = 19, scale = 4)
    private BigDecimal affiliatePercent;

    @Column(name = "student_discount_percent", precision = 19, scale = 4)
    private BigDecimal studentDiscountPercent;

    @Column(nullable = false)
    private boolean isBonus;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (affiliatePercent == null)
            affiliatePercent = BigDecimal.ZERO;
        if (studentDiscountPercent == null)
            studentDiscountPercent = BigDecimal.ZERO;
    }
}
