package com.project.www.tenant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantModuleUpdateRequest {
    private Double amount;
    private String specialRequirements;
    private Double extraCharges;
    private java.time.LocalDate startDate;
    private java.time.LocalDate expiryDate;
}
