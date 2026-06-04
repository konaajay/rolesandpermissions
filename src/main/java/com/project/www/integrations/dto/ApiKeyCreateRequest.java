package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApiKeyCreateRequest {
    @NotBlank
    private String keyName;
    private List<String> permissions;
    private List<String> ipWhitelist;
    private LocalDateTime expiryDate;
}
