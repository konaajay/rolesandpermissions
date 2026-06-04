package com.project.www.dto;

import com.project.www.enums.*;
import java.util.List;
import com.project.www.enums.DiscountType;
import lombok.Data;

@Data
public class CouponRequest {
    private String code;
    private DiscountType discountType;
    private Double discountValue;
    private Double discountCap;
    private String expiryDate;
    private Integer maxUsage;
    private Double minPurchaseAmount;
    private Integer perUserLimit;
    private boolean firstOrderOnly;
    private boolean autoApply;
    private Long affiliateId;
    private Long learnerId;
    private List<Long> courseIds;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    public Double getDiscountCap() { return discountCap; }
    public void setDiscountCap(Double discountCap) { this.discountCap = discountCap; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }
    public Double getMinPurchaseAmount() { return minPurchaseAmount; }
    public void setMinPurchaseAmount(Double minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }
    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }
    public boolean isFirstOrderOnly() { return firstOrderOnly; }
    public void setFirstOrderOnly(boolean firstOrderOnly) { this.firstOrderOnly = firstOrderOnly; }
    public boolean isAutoApply() { return autoApply; }
    public void setAutoApply(boolean autoApply) { this.autoApply = autoApply; }
    public Long getAffiliateId() { return affiliateId; }
    public void setAffiliateId(Long affiliateId) { this.affiliateId = affiliateId; }
    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }
    public List<Long> getCourseIds() { return courseIds; }
    public void setCourseIds(List<Long> courseIds) { this.courseIds = courseIds; }
}
