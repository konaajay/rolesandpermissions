package com.project.www.entity;

import jakarta.persistence.*;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_notifications")
public class PushNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    private String targetChannel;
    private int recipientsCount;
    private java.time.LocalDateTime sentAt;

    private String link;

    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING, SENT, FAILED

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public PushNotification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getTargetChannel() { return targetChannel; }
    public void setTargetChannel(String targetChannel) { this.targetChannel = targetChannel; }

    public int getRecipientsCount() { return recipientsCount; }
    public void setRecipientsCount(int recipientsCount) { this.recipientsCount = recipientsCount; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
