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
public class PurchaseOrderItemDto {
    private Long id;

    @NotBlank(message = "Item Description is required")
    private String itemDescription;

    private String brand;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;
}
