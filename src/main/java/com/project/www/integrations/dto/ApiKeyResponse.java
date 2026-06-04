package com.project.www.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String keyName;
    private String apiKey;
    private String apiSecret;
    private String maskedKey;
    private List<String> permissions;
    private List<String> ipWhitelist;
    private LocalDateTime expiryDate;
    private String status;
    private LocalDateTime createdAt;
}
