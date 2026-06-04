package com.project.www.integrations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GmailSendRequest {
    @NotBlank @Email
    private String to;
    @NotBlank
    private String subject;
    @NotBlank
    private String body;
    private String module;
    private Long referenceId;
}
