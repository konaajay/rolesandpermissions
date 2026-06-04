package com.project.www.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import com.project.www.enums.CampaignChannel;
import com.project.www.enums.CampaignStatus;
import com.project.www.enums.TargetAudience;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignId;

    @Column(name = "campaign_name", nullable = false, length = 150)
    private String campaignName;

    @Column(length = 200)
    private String subject;

    @Column(length = 50)
    private String campaignType;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CampaignStatus status;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetAudience targetAudience;

    @Column(columnDefinition = "TEXT")
    private String audienceFilters;

    @Column(name = "module_type", length = 50)
    private String moduleType;

    @Column(name = "audience_source", length = 50)
    private String audienceSource;

    @jakarta.persistence.ElementCollection(fetch = FetchType.EAGER)
    @jakarta.persistence.CollectionTable(name = "campaign_recipients", joinColumns = @jakarta.persistence.JoinColumn(name = "campaign_id"))
    @Column(name = "email")
    private List<String> recipients = new ArrayList<>();

    private LocalDateTime archivedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_count")
    private int sentCount = 0;

    @Column(name = "failed_count")
    private int failedCount = 0;

    @Column(name = "open_count")
    private int openCount = 0;

    @Column(name = "click_count")
    private int clickCount = 0;
    @OneToMany(mappedBy = "coreCampaign", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<EmailCampaign> emailCampaigns;

    // Standard Getters & Setters - Manual Restoration 🛡️🏁
    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long id) {
        this.campaignId = id;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String name) {
        this.campaignName = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(String type) {
        this.campaignType = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate date) {
        this.startDate = date;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate date) {
        this.endDate = date;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CampaignChannel getChannel() {
        return channel;
    }

    public void setChannel(CampaignChannel channel) {
        this.channel = channel;
    }

    public TargetAudience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(TargetAudience audience) {
        this.targetAudience = audience;
    }

    public String getAudienceFilters() {
        return audienceFilters;
    }

    public void setAudienceFilters(String filters) {
        this.audienceFilters = filters;
    }

    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }

    public String getAudienceSource() { return audienceSource; }
    public void setAudienceSource(String audienceSource) { this.audienceSource = audienceSource; }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public List<EmailCampaign> getEmailCampaigns() {
        return emailCampaigns;
    }

    public void setEmailCampaigns(List<EmailCampaign> emails) {
        this.emailCampaigns = emails;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public int getSentCount() { return sentCount; }
    public void setSentCount(int sentCount) { this.sentCount = sentCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public int getOpenCount() { return openCount; }
    public void setOpenCount(int openCount) { this.openCount = openCount; }
    public int getClickCount() { return clickCount; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
}
