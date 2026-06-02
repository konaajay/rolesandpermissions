package com.project.www.dto;

import lombok.Data;

@Data
public class RequirementItemRequest {
    private String itemName;
    private String brand;
    private Integer quantity;
    private String unit;
}
