package com.project.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private Long id;
    private String name;
    private String code;
    private String domain;
    private String dbName;
    private Boolean active;
    private String adminEmail;
    private String superAdminName;
    private String phone;
    private String status;
    private String subscriptionType;
    private java.time.LocalDate subscriptionStartDate;
    private java.time.LocalDate subscriptionEndDate;
}
