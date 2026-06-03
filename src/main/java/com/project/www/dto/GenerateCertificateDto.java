package com.project.www.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GenerateCertificateDto {
    private Long userId;
    private Long templateId;
    private LocalDateTime issuedDate;
    private LocalDateTime expiryDate;
    private String customHtml;
    private Boolean sendEmail;
}
