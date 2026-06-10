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
public class SubscriptionResponse {
    private Long id;
    private Long planId;
    private String planName;
    private Double amount;
    private String billingInterval;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String paymentReference;
    private Double amountPaid;
    private Double amountPending;
    private String paymentHistory;
    private String createdAt;
}
