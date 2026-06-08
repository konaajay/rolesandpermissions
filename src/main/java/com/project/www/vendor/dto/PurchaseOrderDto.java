package com.project.www.vendor.dto;

import com.project.www.vendor.entity.Vendor;

import jakarta.validation.constraints.Min;
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
public class PurchaseOrderDto {
    private Long id;
    
    private String poNumber;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    private String vendorName;

    @Builder.Default
    private java.util.List<PurchaseOrderItemDto> items = new java.util.ArrayList<>();

    private BigDecimal totalAmount;
    private String amountFormatted;

    private String date; // corresponds to orderDate
    private String deliveryDate;

    @NotBlank(message = "Status is required")
    private String status;

    private String notes;
}
