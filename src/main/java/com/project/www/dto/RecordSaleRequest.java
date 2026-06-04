package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class RecordSaleRequest {
    private String affiliateCode;
    private Long courseId;
    private Long batchId;
    private String orderId;
    private Double amount;
}
