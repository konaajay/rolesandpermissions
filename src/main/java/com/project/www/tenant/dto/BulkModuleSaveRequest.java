package com.project.www.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkModuleSaveRequest {
    private List<BulkModuleItemRequest> modules;
    private String paymentType; // FULL, INSTALLMENT
    private String invoiceType; // NEW_SUBSCRIPTION, RENEWAL, ADDON_MODULE
    private Integer noOfInstallments;
    private Double installmentAmount;
    private Double gstPercentage;
    private String discountType; // FLAT, PERCENTAGE
    private Double discountValue;
    private List<String> installmentDates;
}
