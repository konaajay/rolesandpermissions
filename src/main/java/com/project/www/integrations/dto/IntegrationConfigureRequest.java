package com.project.www.integrations.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class IntegrationConfigureRequest {
    private Long tenantId;

    private String apiKey;
    private String apiSecret;

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private List<String> scopes;

    private String webhookUrl;
    private String environment;
    private Map<String, String> settings;
}
