package com.project.www.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorCategoryDto {
    private Long id;

    @NotBlank(message = "Category Name is required")
    private String name;

    private String description;
    private Boolean active;
}
