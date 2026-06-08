package com.project.www.vendor.dto;

import com.project.www.vendor.entity.Vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorAuditDto {
    private Long id;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    private String vendorName;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Status is required")
    private String status;

    private String auditDate;
    private String nextAudit;
    private String auditor;
    private String findings;
}
