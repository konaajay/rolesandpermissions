package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WhatsappSendRequest {
    @NotBlank
    private String to;
    @NotBlank
    private String message;
    private String module;
    private Long referenceId;
}
