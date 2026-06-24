package com.project.www.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkModuleItemRequest {
    private String moduleName;
    private Double amount;
    private Double extraCharges;
    private String specialRequirements;
    private LocalDate startDate;
    private LocalDate expiryDate;
}
