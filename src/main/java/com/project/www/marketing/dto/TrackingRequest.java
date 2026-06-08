package com.project.www.marketing.dto;

import com.project.www.marketing.dto.TrackingRequest;

import com.project.www.enums.*;

import jakarta.validation.constraints.NotBlank;

public class TrackingRequest {
    @NotBlank(message = "Tracking ID is required")
    private String trackedLinkId;
    
    @NotBlank(message = "Event type is required")
    private String eventType; // PAGE_VIEW, CLICK, LEAD
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    private String metadata;

    public TrackingRequest() {}

    public TrackingRequest(String trackedLinkId, String eventType, String sessionId, String metadata) {
        this.trackedLinkId = trackedLinkId;
        this.eventType = eventType;
        this.sessionId = sessionId;
        this.metadata = metadata;
    }

    public String getTrackedLinkId() { return trackedLinkId; }
    public void setTrackedLinkId(String id) { this.trackedLinkId = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String type) { this.eventType = type; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String id) { this.sessionId = id; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getTid() {
        return trackedLinkId;
    }
}
