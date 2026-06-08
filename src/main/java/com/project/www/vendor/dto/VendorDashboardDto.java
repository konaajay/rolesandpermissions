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
public class VendorDashboardDto {
    private Stats stats;
    private List<SpendChartData> spendData;
    private List<VendorChartData> vendorData;
    private List<Alert> alerts;
    private List<Activity> activities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int activeVendors;
        private int activeContracts;
        private String procurementSpend;
        private int pendingApprovals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendChartData {
        private String name;
        private double spend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorChartData {
        private String name;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String vendor;
        private String issue;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String title;
        private String desc;
        private String time;
    }
}
