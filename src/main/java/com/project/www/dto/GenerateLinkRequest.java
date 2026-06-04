package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GenerateLinkRequest {
    private Long affiliateId;
    private Long courseId;
    private Long batchId;
    private java.math.BigDecimal commissionValue;
    private java.math.BigDecimal studentDiscountValue;
    private LocalDateTime expiresAt;
}
