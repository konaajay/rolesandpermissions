package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetaConfigureRequest {
    @NotBlank
    private String accessToken;
    private String pageId;
    private String formId;
    private String webhookVerifyToken;
    private String environment;
}
