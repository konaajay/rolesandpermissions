package com.project.www.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.www.marketing.service.MarketingAnalyticsService;

@RestController
@RequestMapping("/marketing/admin/analytics")
@RequiredArgsConstructor

public class AdminAnalyticsController {

    private final MarketingAnalyticsService analyticsService;

    // ===== VIEW ANALYTICS =====
    @GetMapping("/track/{tid}")

    public ResponseEntity<?> getStats(@PathVariable String tid) {

        // optional basic validation
        if (tid == null || tid.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid tracking id");
        }

        return ResponseEntity.ok(
                analyticsService.getStatsByTid(tid));
    }

    @GetMapping("/sources")

    public ResponseEntity<?> getSourceStats() {
        return ResponseEntity.ok(analyticsService.getTrafficSourceStats());
    }

    @GetMapping("/funnel")

    public ResponseEntity<?> getFunnelStats() {
        return ResponseEntity.ok(analyticsService.getFunnelStats());
    }

    @GetMapping("/conversion-rate")

    public ResponseEntity<?> getConversionRate() {
        return ResponseEntity.ok(analyticsService.getConversionRate());
    }

    @GetMapping("/campaigns")

    public ResponseEntity<?> getCampaignStats() {
        return ResponseEntity.ok(analyticsService.getCampaignStats());
    }

    @GetMapping("/mediums")

    public ResponseEntity<?> getMediumStats() {
        return ResponseEntity.ok(analyticsService.getMediumStats());
    }
}
