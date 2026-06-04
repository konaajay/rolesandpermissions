package com.project.www.dto;

import com.project.www.enums.*;

import java.time.LocalDate;
import java.math.BigDecimal;

public class CampaignResponseDTO {
    private Long campaignId;
    private String campaignName;
    private String subject;
    private String campaignType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private String status;
    private String emailCampaignStatus; 
    private String description;
    private String channel;
    private String targetAudience;
    private String audienceFilters;
    private String content;

    private String fromName;
    private String fromEmail;
    private String replyTo;
    private String triggerCondition;

    private Long totalImpressions = 0L;
    private Long totalClicks = 0L;
    private Long totalConversions = 0L;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private Double conversionRate = 0.0;
    private BigDecimal costPerClick = BigDecimal.ZERO;

    private Integer totalRecipients = 0;
    private Integer successCount = 0;
    private Integer failedCount = 0;

    // Standard Getters & Setters for 100% Compiler Sync 🛡️🏁
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getCampaignType() { return campaignType; }
    public void setCampaignType(String campaignType) { this.campaignType = campaignType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEmailCampaignStatus() { return emailCampaignStatus; }
    public void setEmailCampaignStatus(String emailCampaignStatus) { this.emailCampaignStatus = emailCampaignStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public String getAudienceFilters() { return audienceFilters; }
    public void setAudienceFilters(String audienceFilters) { this.audienceFilters = audienceFilters; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }
    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }
    public Long getTotalImpressions() { return totalImpressions; }
    public void setTotalImpressions(Long totalImpressions) { this.totalImpressions = totalImpressions; }
    public Long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(Long totalClicks) { this.totalClicks = totalClicks; }
    public Long getTotalConversions() { return totalConversions; }
    public void setTotalConversions(Long totalConversions) { this.totalConversions = totalConversions; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public Double getConversionRate() { return conversionRate; }
    public void setConversionRate(Double conversionRate) { this.conversionRate = conversionRate; }
    public BigDecimal getCostPerClick() { return costPerClick; }
    public void setCostPerClick(BigDecimal costPerClick) { this.costPerClick = costPerClick; }
    public Integer getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(Integer totalRecipients) { this.totalRecipients = totalRecipients; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
}
