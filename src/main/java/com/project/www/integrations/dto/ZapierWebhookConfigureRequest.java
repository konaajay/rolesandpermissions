package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ZapierWebhookConfigureRequest {
    @NotBlank
    private String webhookUrl;
    private List<String> events;
}
