package com.project.www.service;

import com.project.www.enums.*;
import com.project.www.dto.CampaignReportDTO;
import com.project.www.dto.CampaignRequestDTO;
import com.project.www.dto.EmailProviderResult;
import com.project.www.entity.Campaign;
import com.project.www.entity.Lead;
import com.project.www.repository.CampaignRepository;
import com.project.www.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * CampaignService - unified campaign management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final LeadRepository leadRepository;
    private final EmailSenderService emailSenderService;

    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with ID: " + id));
    }

    public Map<String, Object> getMarketingSummary() {
        long totalCampaigns = campaignRepository.count();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCampaigns", totalCampaigns);
        summary.put("status", "ACTIVE");
        return summary;
    }

    public List<Campaign> getAllCampaignEntities() {
        return campaignRepository.findAll();
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    public Campaign saveCampaign(Campaign campaign) {
        return campaignRepository.save(campaign);
    }

    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
    }

    public CampaignReportDTO getCampaignReport(Long campaignId) {
        Campaign campaign = getCampaignById(campaignId);
        CampaignReportDTO report = new CampaignReportDTO();
        report.setCampaignId(campaign.getCampaignId());
        report.setCampaignName(campaign.getCampaignName());
        return report;
    }

    public Map<String, Object> getGlobalAnalyticsReport() {
        List<Campaign> campaigns = getAllCampaignEntities();
        Map<String, Object> report = new HashMap<>();
        report.put("campaignCount", campaigns.size());
        report.put("generatedAt", java.time.LocalDateTime.now());
        return report;
    }

    // Phase 1 - processCampaign
    public Campaign processCampaign(CampaignRequestDTO request) {
        Campaign campaign = new Campaign();
        mapDtoToEntity(request, campaign);
        
        Campaign saved = campaignRepository.save(campaign);
        
        if (CampaignStatus.ACTIVE.equals(saved.getStatus())) {
            executeCampaignEmails(saved, request.getRecipients());
        }
        
        return saved;
    }

    // Phase 1 - processCampaignUpdate
    public Campaign processCampaignUpdate(Long id, CampaignRequestDTO request) {
        Campaign campaign = getCampaignById(id);
        mapDtoToEntity(request, campaign);
        
        Campaign saved = campaignRepository.save(campaign);
        
        if (CampaignStatus.ACTIVE.equals(saved.getStatus())) {
            executeCampaignEmails(saved, request.getRecipients());
        }
        
        return saved;
    }

    @Async
    public void executeCampaignEmails(Campaign campaign, List<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients for campaign {}", campaign.getCampaignId());
            campaign.setStatus(CampaignStatus.FAILED);
            campaignRepository.save(campaign);
            return;
        }

        campaign.setStatus(CampaignStatus.SENDING);
        campaign = campaignRepository.save(campaign);

        int sent = campaign.getSentCount();
        int failed = campaign.getFailedCount();

        for (String email : recipients) {
            EmailProviderResult result = emailSenderService.sendCampaignEmail(campaign, email);
            if (result.isSuccess()) {
                sent++;
            } else {
                failed++;
            }
        }

        campaign.setSentCount(sent);
        campaign.setFailedCount(failed);
        campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);
    }

    private void mapDtoToEntity(CampaignRequestDTO dto, Campaign entity) {
        if (dto.getCampaignName() != null) entity.setCampaignName(dto.getCampaignName());
        if (dto.getSubject() != null) entity.setSubject(dto.getSubject());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getCampaignType() != null) entity.setCampaignType(dto.getCampaignType());
        if (dto.getBudget() != null) entity.setBudget(dto.getBudget());
        else if (entity.getBudget() == null) entity.setBudget(BigDecimal.ZERO);
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        
        if (dto.getChannel() != null) {
            try { entity.setChannel(CampaignChannel.valueOf(dto.getChannel())); } catch (Exception e) { entity.setChannel(CampaignChannel.EMAIL); }
        } else if (entity.getChannel() == null) entity.setChannel(CampaignChannel.EMAIL);

        if (dto.getTargetAudience() != null) {
            try { entity.setTargetAudience(TargetAudience.valueOf(dto.getTargetAudience())); } catch (Exception e) { entity.setTargetAudience(TargetAudience.CUSTOM); }
        } else if (entity.getTargetAudience() == null) entity.setTargetAudience(TargetAudience.CUSTOM);
        
        if (dto.getScheduledAt() != null) entity.setScheduledAt(dto.getScheduledAt());
        
        if (dto.getStatus() != null) {
            try { entity.setStatus(CampaignStatus.valueOf(dto.getStatus())); } catch (Exception e) { entity.setStatus(CampaignStatus.DRAFT); }
        } else if (entity.getStatus() == null) entity.setStatus(CampaignStatus.DRAFT);

        if (dto.getRecipients() != null && !dto.getRecipients().isEmpty()) {
            entity.setRecipients(new java.util.ArrayList<>(dto.getRecipients()));
        }

        if (dto.getModuleType() != null) entity.setModuleType(dto.getModuleType());
        if (dto.getAudienceSource() != null) entity.setAudienceSource(dto.getAudienceSource());
    }
}
