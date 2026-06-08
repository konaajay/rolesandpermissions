package com.project.www.marketing.dto;

import com.project.www.marketing.dto.LandingPageRequest;

import com.project.www.enums.*;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LandingPageRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Slug is required")
    private String slug;
    
    private String headline;
    private String subtitle;
    
    // Support for universal module selection
    private String moduleType;
    private String landingPageType;
    private String description;
    
    // Optional now depending on moduleType
    private BigDecimal price;
    
    private BigDecimal adBudget;
    private String videoUrl;
    private List<String> features;
    private String ctaText;
}
