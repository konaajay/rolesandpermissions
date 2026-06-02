package com.project.www.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorInvoiceDto {
    private Long id;
    private String invoiceNumber;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    private String vendorName;

    private BigDecimal amountValue;
    private String amount; // formatted e.g. "$12,000"

    private String poRef;
    private String date; // Maps to invoiceDate
    private String dueDate;

    @NotBlank(message = "Status is required")
    private String status;

    private String notes;
    private String receiptUrl;
}
