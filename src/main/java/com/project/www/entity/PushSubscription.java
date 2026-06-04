package com.project.www.entity;

import jakarta.persistence.*;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscriptions", indexes = {
        @Index(name = "idx_push_learner", columnList = "learner_id"),
        @Index(name = "idx_push_token", columnList = "device_token")
})
public class PushSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "learner_id", nullable = false)
    private Long learnerId;

    @Column(name = "device_token", nullable = false, unique = true)
    private String deviceToken;

    @Column(length = 20)
    private String platform; // WEB, ANDROID, IOS

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public PushSubscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
