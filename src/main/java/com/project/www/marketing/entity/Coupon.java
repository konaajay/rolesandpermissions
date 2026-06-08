package com.project.www.marketing.entity;

import com.project.www.entity.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.project.www.enums.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    @Column(name = "discount_cap")
    private Double discountCap;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "used_count")
    private int usedCount = 0;

    @Column(name = "min_purchase_amount")
    private double minPurchaseAmount = 0.0;

    @Column(name = "per_user_limit")
    private int perUserLimit = 1;

    @Column(name = "is_first_order_only")
    private boolean isFirstOrderOnly = false;

    @Column(name = "auto_apply")
    private boolean autoApply = false;

    @Column(name = "affiliate_id")
    private Long affiliateId;

    @Column(name = "learner_id")
    private Long learnerId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CouponStatus status = CouponStatus.ACTIVE;

    private boolean deleted = false;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    public Double getDiscountCap() { return discountCap; }
    public void setDiscountCap(Double discountCap) { this.discountCap = discountCap; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public double getMinPurchaseAmount() { return minPurchaseAmount; }
    public void setMinPurchaseAmount(double minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }
    public int getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(int perUserLimit) { this.perUserLimit = perUserLimit; }
    public boolean isFirstOrderOnly() { return isFirstOrderOnly; }
    public void setFirstOrderOnly(boolean isFirstOrderOnly) { this.isFirstOrderOnly = isFirstOrderOnly; }
    public boolean isAutoApply() { return autoApply; }
    public void setAutoApply(boolean autoApply) { this.autoApply = autoApply; }
    public Long getAffiliateId() { return affiliateId; }
    public void setAffiliateId(Long affiliateId) { this.affiliateId = affiliateId; }
    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }
    public CouponStatus getStatus() { return status; }
    public void setStatus(CouponStatus status) { this.status = status; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
