package com.project.www.marketing.dto;

import com.project.www.marketing.dto.CreateLeadRequest;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class CreateLeadRequest {
    private String name;
    private String mobile;
    private String email;

    private Long batchId;

}
