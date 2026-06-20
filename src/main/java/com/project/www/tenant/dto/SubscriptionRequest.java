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
    private java.time.LocalDate startDate; // Custom start date
    private java.time.LocalDate endDate; // Custom end date
    private String paymentReference;
    private Double amountPaid;
    private Double amountPending;
    private String paymentHistory;
    private java.util.Set<String> customModules; // For overriding or custom plans
    
    private java.util.List<ModuleAssignment> moduleAssignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleAssignment {
        private String moduleName;
        private Double amount;
        private String paymentMethod;
        private String specialRequirements;
        private Double extraCharges;
    }
}
