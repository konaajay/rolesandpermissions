package com.project.www.integrations.service;

import java.util.Map;

import com.project.www.integrations.dto.IntegrationTestResponse;
import com.project.www.integrations.dto.OAuthConnectResponse;
import com.project.www.integrations.dto.ZoomMeetingRequest;

public interface ZoomService {
    OAuthConnectResponse buildConnectUrl();
    String handleCallback(String authCode, String state);
    Map<String, Object> getStatus();
    Map<String, Object> createMeeting(ZoomMeetingRequest request);
    Map<String, Object> updateMeeting(String meetingId, ZoomMeetingRequest request);
    void deleteMeeting(String meetingId);
    IntegrationTestResponse testConnection();
    void disconnect();
}
