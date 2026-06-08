package com.project.www.accessmanagement.dto;

import com.project.www.accessmanagement.dto.PublicVerificationDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PublicVerificationDto {
    private String certificateNo;
    private String employeeName;
    private String certificateType;
    private LocalDateTime issuedDate;
    private String issuedBy;
    private String status;
}
