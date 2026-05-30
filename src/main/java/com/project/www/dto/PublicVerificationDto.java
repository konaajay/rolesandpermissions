package com.project.www.dto;

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
