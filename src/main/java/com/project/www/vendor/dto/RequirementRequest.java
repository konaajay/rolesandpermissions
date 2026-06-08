package com.project.www.vendor.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RequirementRequest {
    private String description;
    private Long vendorId;
    private LocalDate requiredDate;
    private LocalDate returnDate;
    private String requirementType;
    private List<RequirementItemRequest> items;
}
