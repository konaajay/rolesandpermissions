package com.project.www.marketing.entity;

import com.project.www.entity.*;

import com.project.www.enums.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "campaign_performance", indexes = {
        @Index(name = "idx_perf_campaign_date", columnList = "campaign_id, recordedDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    public Campaign getCampaign() { return campaign; }

    private Long impressions = 0L;
    private Long clicks = 0L;
    private Long conversions = 0L;

    @Column(precision = 19, scale = 4)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(nullable = false)
    private java.time.LocalDate recordedDate;

    @jakarta.persistence.Transient
    public java.math.BigDecimal getCtr() {
        if (impressions == null || impressions == 0)
            return java.math.BigDecimal.ZERO;
        return java.math.BigDecimal.valueOf(clicks)
                .divide(java.math.BigDecimal.valueOf(impressions), 4, java.math.RoundingMode.HALF_UP);
    }

    @jakarta.persistence.Transient
    public java.math.BigDecimal getCpa() {
        if (conversions == null || conversions == 0)
            return java.math.BigDecimal.ZERO;
        return cost.divide(java.math.BigDecimal.valueOf(conversions), 4, java.math.RoundingMode.HALF_UP);
    }
}
