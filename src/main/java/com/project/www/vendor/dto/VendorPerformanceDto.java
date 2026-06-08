package com.project.www.vendor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorPerformanceDto {
    private String topVendorName;
    private List<RadarMetric> scorecard;
    private List<KpiMetric> kpis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RadarMetric {
        private String subject;
        private int A;
        private int fullMark;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiMetric {
        private String label;
        private double val;
        private String color;
    }
}
