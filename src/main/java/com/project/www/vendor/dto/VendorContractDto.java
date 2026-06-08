package com.project.www.vendor.dto;

import com.project.www.vendor.entity.Vendor;

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
public class VendorContractDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    private String vendorName; // Used as 'vendor' in frontend

    private BigDecimal amountValue;
    private String amount; // formatted string e.g., "$120,000"

    private String startDate;
    private String expires;

    @NotBlank(message = "Status is required")
    private String status;

    private String documentUrl;
    
    private String notes;
}
