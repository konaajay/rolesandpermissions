package com.project.www.marketing.service;

import com.project.www.marketing.service.MarketingAnalyticsService;

import com.project.www.marketing.entity.TrafficEvent;

import com.project.www.enums.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.www.marketing.entity.TrafficEvent;
import com.project.www.marketing.repository.MarketingTrafficEventRepository;

@Service("marketingAnalyticsService")
public class MarketingAnalyticsService {

    @Autowired
    private MarketingTrafficEventRepository trafficEventRepository;

    public Map<String, Object> getStatsByTid(String tid) {
        long uniqueClicks = trafficEventRepository.countUniqueClicks(tid, "PAGE_VIEW");
        long leads = trafficEventRepository.countLeadsByTid(tid);
        
        double conversionRate = uniqueClicks == 0 ? 0 : (double) leads / uniqueClicks * 100;
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("tid", tid);
        stats.put("uniqueClicks", uniqueClicks);
        stats.put("leads", leads);
        stats.put("conversionRate", conversionRate);
        
        return stats;
    }


    private Map<String, Long> safeProcess(List<Object[]> results) {
        if (results == null) return java.util.Collections.emptyMap();
        return results.stream()
                .filter(row -> row != null && row.length >= 2 && row[0] != null)
                .collect(Collectors.toMap(
                        row -> String.valueOf(row[0]),
                        row -> {
                            try {
                                return row[1] != null ? ((Number) row[1]).longValue() : 0L;
                            } catch (Exception e) {
                                return 0L;
                            }
                        },
                        (existing, replacement) -> existing + replacement));
    }

    public Map<String, Long> getTrafficSourceStats() {
        try {
            return safeProcess(trafficEventRepository.countUniqueBySource());
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    public Map<String, Long> getFunnelStats() {
        try {
            return safeProcess(trafficEventRepository.countUniqueByEventType());
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    public @org.springframework.lang.NonNull TrafficEvent recordEvent(
            @org.springframework.lang.NonNull TrafficEvent event) {
        return trafficEventRepository.save(event);
    }

    public Double getConversionRate() {
        Map<String, Long> stats = getFunnelStats();
        Long clicks = stats.getOrDefault("CLICK", 0L) + stats.getOrDefault("PAGE_VIEW", 0L);
        Long purchases = stats.getOrDefault("PURCHASE", 0L);

        if (clicks == 0)
            return 0.0;
        return (purchases.doubleValue() / clicks.doubleValue()) * 100;
    }

    public Map<String, Long> getCampaignStats() {
        try {
            return safeProcess(trafficEventRepository.countUniqueByUtmCampaign());
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    public Map<String, Long> getMediumStats() {
        try {
            return safeProcess(trafficEventRepository.countUniqueByUtmMedium());
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    public List<Object[]> getCampaignFunnel() {
        return trafficEventRepository.getCampaignFunnelStats();
    }
}
