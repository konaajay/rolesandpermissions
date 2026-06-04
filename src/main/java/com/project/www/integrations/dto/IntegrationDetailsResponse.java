package com.project.www.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationDetailsResponse {
    private String code;
    private String name;
    private String description;
    private boolean enabled;
    private boolean connected;
    private String health;
    private String environment;
    private String webhookUrl;
    private String apiKeyMasked;
    private String apiSecretMasked;
    private String lastSynced;
}
