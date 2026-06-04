package com.project.www.dto;

import com.project.www.enums.*;

import jakarta.validation.constraints.DecimalMin;
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
public class PayoutRequestDTO {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Minimum payout request must be at least 1.0")
    private BigDecimal amount;
}
