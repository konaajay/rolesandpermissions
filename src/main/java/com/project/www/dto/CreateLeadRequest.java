package com.project.www.dto;

import com.project.www.enums.*;

import lombok.Data;

@Data
public class CreateLeadRequest {
    private String name;
    private String mobile;
    private String email;
    private Long courseId;
    private Long batchId;
    private String referralCode;
}
