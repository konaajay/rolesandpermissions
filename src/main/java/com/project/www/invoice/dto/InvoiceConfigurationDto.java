package com.project.www.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceConfigurationDto {
    private Long id;
    
    @NotBlank(message = "Invoice name is required")
    private String invoiceName;
    
    private String invoicePrefix;
    private String invoiceNumberFormat;
    private String companyLogo;
    private String companyDetails;
    private String gstTaxDetails;
    private String termsConditions;
    private Boolean active;
    private String targetModule;
}
