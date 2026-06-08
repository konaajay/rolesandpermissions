package com.project.www.marketing.dto;

import com.project.www.marketing.dto.CouponApplyRequest;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class CouponApplyRequest {
    private String code;
    private Long courseId;
    private Double amount;
    private Long learnerId; // Optional: can be null
}
