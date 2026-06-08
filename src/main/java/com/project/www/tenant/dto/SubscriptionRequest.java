package com.project.www.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    private Long planId;
    private String planName; // Only if custom plan
    private Double amount;
    private String billingInterval; // MONTHLY, YEARLY
    private Integer durationDays; // Fallback
    private java.time.LocalDate endDate; // Custom end date
    private String paymentReference;
    private java.util.Set<String> customModules; // For overriding or custom plans
}
