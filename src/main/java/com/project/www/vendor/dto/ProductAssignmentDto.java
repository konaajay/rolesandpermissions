package com.project.www.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAssignmentDto {
    private Long id;
    private Long receivedProductId;
    private String productName;
    private Long userId;
    private String userName;
    private Integer quantity;
    private LocalDateTime assignedAt;
    private Long assignedBy;
    private String status;
    private String assetIdentifier;
    private String itemType;
}
