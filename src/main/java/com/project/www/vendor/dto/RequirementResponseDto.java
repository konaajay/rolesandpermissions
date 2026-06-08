package com.project.www.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequirementResponseDto {
    private Long id;
    private String description;
    private String requirementType;
    private String status;
    private LocalDate requiredDate;
    private LocalDate returnDate;
    
    private VendorDto vendor;
    private List<RequirementItemDto> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorDto {
        private Long id;
        private String vendorName;
        private String companyName;
        private String email;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequirementItemDto {
        private Long id;
        private String itemName;
        private String brand;
        private Integer quantity;
        private String unit;
    }
}
