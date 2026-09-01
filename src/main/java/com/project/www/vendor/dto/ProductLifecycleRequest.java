package com.project.www.vendor.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ProductLifecycleRequest {
    private String description;
}
