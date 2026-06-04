package com.project.www.entity;

import jakarta.persistence.*;
import com.project.www.enums.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "email_campaigns")
public class EmailCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private EmailCampaignStatus status = EmailCampaignStatus.DRAFT;

    private String channel; 
    private String campaignType; 
    private String triggerEvent; 

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "from_email")
    private String fromEmail;

    @Column(name = "reply_to")
    private String replyTo;

    @Column(name = "total_recipients")
    private int totalRecipients;

    @Column(name = "success_count")
    private int successCount;

    @Column(name = "failed_count")
    private int failedCount;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "core_campaign_id")
    private Campaign coreCampaign;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailRecipient> recipients;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Standard Getters & Setters - Manual Restoration 🛡️🏁
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public EmailCampaignStatus getStatus() { return status; }
    public void setStatus(EmailCampaignStatus status) { this.status = status; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getCampaignType() { return campaignType; }
    public void setCampaignType(String campaignType) { this.campaignType = campaignType; }
    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }
    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
    public int getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(int totalRecipients) { this.totalRecipients = totalRecipients; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public Campaign getCoreCampaign() { return coreCampaign; }
    public void setCoreCampaign(Campaign coreCampaign) { this.coreCampaign = coreCampaign; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<EmailRecipient> getRecipients() { return recipients; }
}
