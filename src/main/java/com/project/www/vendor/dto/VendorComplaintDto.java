package com.project.www.vendor.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorComplaintDto {
    private Long   id;
    private Long   vendorId;
    private String vendorName;
    private String productOrService;
    private String complaintType;
    private String severity;
    private String description;
    private String status;
    private String dateReported;
    private String resolvedDate;
    private String resolution;
}
