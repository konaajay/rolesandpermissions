package com.project.www.entity;

import java.math.BigDecimal;
import com.project.www.enums.*;
import java.time.LocalDateTime;

import com.project.www.enums.RewardStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "referral_usage",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"referredUserId", "courseId"})},
       indexes = {@Index(name = "idx_referral_usage_user_course", columnList = "referredUserId, courseId")}
)
public class ReferralUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "referral_code_id")
    private ReferralCode referralCode;

    private Long referrerUserId;

    private Long referredUserId;

    private Long courseId;

    private BigDecimal rewardAmount;

    @Enumerated(EnumType.STRING)
    private RewardStatus rewardStatus;

    private LocalDateTime createdAt;

    public ReferralUsage() {}

    public ReferralUsage(Long id, ReferralCode referralCode, Long referrerUserId, Long referredUserId, Long courseId, BigDecimal rewardAmount, RewardStatus rewardStatus, LocalDateTime createdAt) {
        this.id = id;
        this.referralCode = referralCode;
        this.referrerUserId = referrerUserId;
        this.referredUserId = referredUserId;
        this.courseId = courseId;
        this.rewardAmount = rewardAmount;
        this.rewardStatus = rewardStatus;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReferralCode getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(ReferralCode referralCode) {
        this.referralCode = referralCode;
    }

    public Long getReferrerUserId() {
        return referrerUserId;
    }

    public void setReferrerUserId(Long referrerUserId) {
        this.referrerUserId = referrerUserId;
    }

    public Long getReferredUserId() {
        return referredUserId;
    }

    public void setReferredUserId(Long referredUserId) {
        this.referredUserId = referredUserId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(BigDecimal rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public RewardStatus getRewardStatus() {
        return rewardStatus;
    }

    public void setRewardStatus(RewardStatus rewardStatus) {
        this.rewardStatus = rewardStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
