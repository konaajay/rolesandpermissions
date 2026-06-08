package com.project.www.marketing.service;

import com.project.www.marketing.service.InteractionService;

import com.project.www.marketing.entity.Interaction;

import com.project.www.marketing.entity.CampaignPerformance;

import com.project.www.enums.*;

import com.project.www.marketing.entity.CampaignPerformance;
import com.project.www.marketing.entity.Interaction;
import com.project.www.marketing.repository.InteractionRepository;
import com.project.www.marketing.repository.PerformanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepository interactionRepository;
    @Autowired
    private PerformanceRepository performanceRepository;

    @SuppressWarnings("null")
    @Transactional
    public @org.springframework.lang.NonNull Interaction recordInteraction(
            @org.springframework.lang.NonNull Interaction interaction) {
        interaction.setTimestamp(LocalDateTime.now());
        Interaction saved = interactionRepository.save(interaction);
        updatePerformance(interaction);
        return saved;
    }

    public List<Interaction> getByCampaign(Long campaignId) {
        return interactionRepository.findByCampaignCampaignId(campaignId);
    }

    public List<Interaction> getByCustomer(String customerEmail) {
        return interactionRepository.findByCustomerEmail(customerEmail);
    }

    @SuppressWarnings("null")
    private void updatePerformance(Interaction interaction) {
        LocalDate today = LocalDate.now();
        Long campaignId = interaction.getCampaign().getCampaignId();

        CampaignPerformance performance = performanceRepository
                .findByCampaignCampaignIdAndRecordedDate(campaignId, today)
                .orElseGet(() -> {
                    CampaignPerformance p = new CampaignPerformance();
                    p.setCampaign(interaction.getCampaign());
                    p.setRecordedDate(today);
                    return p;
                });

        if ("CLICK".equalsIgnoreCase(interaction.getActionType())) {
            performance.setClicks(performance.getClicks() + 1);
        } else if ("VIEW".equalsIgnoreCase(interaction.getActionType())) {
            performance.setImpressions(performance.getImpressions() + 1);
        } else if ("CONVERSION".equalsIgnoreCase(interaction.getActionType())) {
            performance.setConversions(performance.getConversions() + 1);
        }

        performanceRepository.save(performance);
        return;
    }
}
