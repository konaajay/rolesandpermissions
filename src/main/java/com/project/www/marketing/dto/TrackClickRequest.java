package com.project.www.marketing.dto;

import com.project.www.marketing.dto.TrackClickRequest;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class TrackClickRequest {
    private String affiliateCode;
    private Long batchId;
}
