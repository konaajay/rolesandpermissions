package com.project.www.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_events", indexes = {
        @Index(name = "idx_te_tid", columnList = "tracked_link_id"),
        @Index(name = "idx_te_session", columnList = "session_id"),
        @Index(name = "idx_te_event_type", columnList = "event_type")
})
public class TrafficEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracked_link_id")
    private String trackedLinkId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "utm_source")
    private String utmSource;

    @Column(name = "utm_campaign")
    private String utmCampaign;

    @Column(name = "utm_medium")
    private String utmMedium;

    @Column(name = "source")
    private String source;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackedLinkId() { return trackedLinkId; }
    public void setTrackedLinkId(String trackedLinkId) { this.trackedLinkId = trackedLinkId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getUtmSource() { return utmSource; }
    public void setUtmSource(String utmSource) { this.utmSource = utmSource; }
    public String getUtmCampaign() { return utmCampaign; }
    public void setUtmCampaign(String utmCampaign) { this.utmCampaign = utmCampaign; }
    public String getUtmMedium() { return utmMedium; }
    public void setUtmMedium(String utmMedium) { this.utmMedium = utmMedium; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
