package com.project.www.service;

import com.project.www.enums.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.www.dto.TrackedLinkAnalyticsDTO;
import com.project.www.entity.TrackedLink;
import com.project.www.repository.LeadRepository;
import com.project.www.repository.TrackedLinkRepository;
import com.project.www.repository.MarketingTrafficEventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TrackedLinkService {

    private static final Logger log = LoggerFactory.getLogger(TrackedLinkService.class);

    @Autowired
    private TrackedLinkRepository repository;

    @Autowired
    private MarketingTrafficEventRepository trafficEventRepository;

    @Autowired
    private LeadRepository leadRepository;

    public TrackedLink saveLink(TrackedLink link) {
        if (link.getTrackedLinkId() == null) {
            link.setTrackedLinkId("TRK-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        }
        if (link.getTimestamp() == null) {
            link.setTimestamp(java.time.LocalDateTime.now());
        }
        return repository.save(link);
    }

    public List<TrackedLink> getAllLinks() {
        return repository.findAllByOrderByTimestampDesc();
    }

    public List<TrackedLinkAnalyticsDTO> getAllLinksWithAnalytics() {
        List<TrackedLink> links = repository.findAllByOrderByTimestampDesc();
        log.info("Aggregating analytics for {} tracked links", links.size());
        return links.stream().map(link -> {

            long clicks = trafficEventRepository.countByUtmSourceAndUtmCampaignAndEventType(
                    link.getSource(), link.getCampaign(), "CLICK");
            long views = trafficEventRepository.countByUtmSourceAndUtmCampaignAndEventType(
                    link.getSource(), link.getCampaign(), "PAGE_VIEW");
            long signups = leadRepository.countByUtmSourceAndUtmCampaign(
                    link.getSource(), link.getCampaign());

            log.debug("Link {}: source={}, campaign={} | clicks={}, views={}, signups={}",
                    link.getId(), link.getSource(), link.getCampaign(), clicks, views, signups);

            return TrackedLinkAnalyticsDTO.builder()
                    .id(link.getId())
                    .landingSlug(link.getLandingSlug())
                    .source(link.getSource())
                    .medium(link.getMedium())
                    .campaign(link.getCampaign())
                    .generatedLink(link.getGeneratedLink())
                    .adBudget(link.getAdBudget())
                    .timestamp(link.getTimestamp())
                    .clicks(clicks)
                    .views(views)
                    .signups(signups)
                    .build();
        }).collect(Collectors.toList());
    }

    public void deleteLink(Long id) {
        repository.deleteById(id);
    }
}
