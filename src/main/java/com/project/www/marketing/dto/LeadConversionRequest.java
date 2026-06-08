package com.project.www.marketing.dto;

import com.project.www.marketing.dto.LeadConversionRequest;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class LeadConversionRequest {
    private Long studentId;
    private java.math.BigDecimal batchPrice;
}
