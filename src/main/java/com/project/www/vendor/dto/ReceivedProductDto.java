package com.project.www.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivedProductDto {
    private Long id;
    private Long vendorId;
    private String vendorName;
    private Long requirementId;
    private Long requirementItemId;
    private String productName;
    private Integer requiredQuantity;
    private Integer receivedQuantity;
    private Integer assignedQuantity;
    private Integer availableQuantity;
    private String status;
}
