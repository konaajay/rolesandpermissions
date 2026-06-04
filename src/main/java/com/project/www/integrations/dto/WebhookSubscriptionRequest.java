package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class WebhookSubscriptionRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String webhookUrl;
    private List<String> events;
    private Boolean enabled;
}
