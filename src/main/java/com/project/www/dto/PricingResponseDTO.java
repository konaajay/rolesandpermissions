package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingResponseDTO {

    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    private BigDecimal discountPercent;
    private String referralCode;

    private boolean discountApplied;
}
