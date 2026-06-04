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
public class WalletConfigDTO {
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal defaultMinPayoutAmount;

    private BigDecimal maxPayoutAmount;
    private boolean studentWithdrawalEnabled;
    private boolean affiliateWithdrawalEnabled;
    private int maxPendingPayouts;
}
