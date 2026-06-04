package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WhatsappConfigureRequest {
    @NotBlank
    private String phoneNumberId;
    private String businessAccountId;
    @NotBlank
    private String accessToken;
    private String webhookVerifyToken;
    private String defaultCountryCode;
    private String environment;
}
