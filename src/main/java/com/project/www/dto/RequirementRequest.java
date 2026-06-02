package com.project.www.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RequirementRequest {
    private String description;
    private Long vendorId;
    private LocalDate requiredDate;
    private List<RequirementItemRequest> items;
}
