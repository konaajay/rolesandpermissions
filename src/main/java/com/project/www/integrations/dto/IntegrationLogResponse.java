package com.project.www.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationLogResponse {
    private String date;
    private String integration;
    private String event;
    private String action;
    private String status;
    private Integer httpStatus;
    private String errorMessage;
    private Integer retryCount;
}
