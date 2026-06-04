package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashfreeCreateOrderRequest {
    @NotBlank
    private String orderId;
    @NotNull
    private BigDecimal amount;
    private String currency = "INR";
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String module;
    private Long referenceId;
}
