package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawalRequest {
    private Long userId;
    private BigDecimal amount;
}
