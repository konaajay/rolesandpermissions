package com.project.www.dto;

import com.project.www.enums.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedLinkAnalyticsDTO {
    private Long id;
    private String landingSlug;
    private String source;
    private String medium;
    private String campaign;
    private String generatedLink;
    private BigDecimal adBudget;
    private LocalDateTime timestamp;
    private long clicks;
    private long views;
    private long signups;
}
