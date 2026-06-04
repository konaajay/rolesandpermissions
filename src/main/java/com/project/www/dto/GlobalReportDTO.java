package com.project.www.dto;

import com.project.www.enums.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.project.www.enums.CampaignChannel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalReportDTO {
    private Map<CampaignChannel, BigDecimal> channelRevenue;
    private Map<CampaignChannel, Double> channelRoi;
    private List<CampaignPerformanceGoalDTO> performanceGoalVsActual;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignPerformanceGoalDTO {
        private String quarter; // Q1, Q2, etc.
        private BigDecimal goal;
        private BigDecimal actualRevenue;
    }
}
