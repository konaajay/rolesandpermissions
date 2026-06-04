package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CashfreeConfigureRequest {
    @NotBlank
    private String appId;
    @NotBlank
    private String secretKey;
    private String environment;
    private String returnUrl;
    private String notifyUrl;
}
