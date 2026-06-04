package com.project.www.dto;

import com.project.www.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchasedCourseResponse {
    private Long courseId;
    private Long batchId;
    private String courseName;
    private BigDecimal purchaseAmount;
    private LocalDateTime purchaseDate;
    private String referralCode;
    private boolean hasReferralLink;

    public PurchasedCourseResponse() {}

    public PurchasedCourseResponse(Long courseId, Long batchId, String courseName, BigDecimal purchaseAmount, LocalDateTime purchaseDate, String referralCode, boolean hasReferralLink) {
        this.courseId = courseId;
        this.batchId = batchId;
        this.courseName = courseName;
        this.purchaseAmount = purchaseAmount;
        this.purchaseDate = purchaseDate;
        this.referralCode = referralCode;
        this.hasReferralLink = hasReferralLink;
    }

    // Manual Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public BigDecimal getPurchaseAmount() { return purchaseAmount; }
    public void setPurchaseAmount(BigDecimal purchaseAmount) { this.purchaseAmount = purchaseAmount; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public boolean isHasReferralLink() { return hasReferralLink; }
    public void setHasReferralLink(boolean hasReferralLink) { this.hasReferralLink = hasReferralLink; }

    // Manual Builder Pattern
    public static PurchasedCourseResponseBuilder builder() {
        return new PurchasedCourseResponseBuilder();
    }

    public static class PurchasedCourseResponseBuilder {
        private Long courseId;
        private Long batchId;
        private String courseName;
        private BigDecimal purchaseAmount;
        private LocalDateTime purchaseDate;
        private String referralCode;
        private boolean hasReferralLink;

        public PurchasedCourseResponseBuilder courseId(Long courseId) { this.courseId = courseId; return this; }
        public PurchasedCourseResponseBuilder batchId(Long batchId) { this.batchId = batchId; return this; }
        public PurchasedCourseResponseBuilder courseName(String courseName) { this.courseName = courseName; return this; }
        public PurchasedCourseResponseBuilder purchaseAmount(BigDecimal purchaseAmount) { this.purchaseAmount = purchaseAmount; return this; }
        public PurchasedCourseResponseBuilder purchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; return this; }
        public PurchasedCourseResponseBuilder referralCode(String referralCode) { this.referralCode = referralCode; return this; }
        public PurchasedCourseResponseBuilder hasReferralLink(boolean hasReferralLink) { this.hasReferralLink = hasReferralLink; return this; }

        public PurchasedCourseResponse build() {
            return new PurchasedCourseResponse(courseId, batchId, courseName, purchaseAmount, purchaseDate, referralCode, hasReferralLink);
        }
    }
}
