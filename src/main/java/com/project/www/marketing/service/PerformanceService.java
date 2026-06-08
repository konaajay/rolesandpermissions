package com.project.www.marketing.service;

import com.project.www.marketing.service.PerformanceService;

import com.project.www.marketing.entity.CampaignPerformance;

import com.project.www.enums.*;

import com.project.www.marketing.entity.CampaignPerformance;
import com.project.www.marketing.repository.PerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PerformanceService {

    @Autowired
    private PerformanceRepository performanceRepository;

    public List<CampaignPerformance> getByCampaign(Long campaignId) {
        return performanceRepository.findAll().stream()
                .filter(p -> p.getCampaign().getCampaignId().equals(campaignId))
                .toList();
    }

    public @org.springframework.lang.NonNull CampaignPerformance save(
            @org.springframework.lang.NonNull CampaignPerformance performance) {
        return performanceRepository.save(performance);
    }
}
