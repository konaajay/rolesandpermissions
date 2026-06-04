package com.project.www.integrations.service;

import com.project.www.integrations.dto.IntegrationTestResponse;
import com.project.www.integrations.dto.OAuthConnectResponse;

public interface GoogleOAuthService {
    OAuthConnectResponse buildConnectUrl();
    String handleCallback(String authCode, String state);
    IntegrationTestResponse testConnection();
    String getValidAccessToken();
}
