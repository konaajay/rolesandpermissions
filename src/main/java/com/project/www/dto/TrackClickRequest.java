package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class TrackClickRequest {
    private String affiliateCode;
    private Long batchId;
}
