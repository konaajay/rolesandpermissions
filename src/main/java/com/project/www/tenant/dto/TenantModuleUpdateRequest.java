package com.project.www.tenant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantModuleUpdateRequest {
    private Double amount;
    private String paymentMethod;
    private String specialRequirements;
    private Double extraCharges;
}
