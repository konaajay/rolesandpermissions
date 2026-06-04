package com.project.www.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyUsageLogResponse {
    private String date;
    private String endpoint;
    private String method;
    private String ipAddress;
    private String status;
}
