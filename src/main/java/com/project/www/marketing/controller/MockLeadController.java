package com.project.www.marketing.controller;

import com.project.www.marketing.entity.MarketingLead;
import com.project.www.marketing.repository.MarketingLeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@Slf4j
public class MockLeadController {

    private final MarketingLeadRepository marketingLeadRepository;

    public MockLeadController(MarketingLeadRepository marketingLeadRepository) {
        this.marketingLeadRepository = marketingLeadRepository;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Map<String, Object>> captureLead(@RequestBody Map<String, Object> data) {
        log.info("Received lead data: {}", data);
        try {
            MarketingLead lead = MarketingLead.builder()
                    .name((String) data.get("name"))
                    .email((String) data.get("email"))
                    .phone((String) data.get("phone"))
                    .courseInterest((String) data.get("courseInterest"))
                    .source((String) data.get("source"))
                    .utmSource((String) data.get("utmSource"))
                    .utmMedium((String) data.get("utmMedium"))
                    .utmCampaign((String) data.get("utmCampaign"))
                    .sessionId((String) data.get("sessionId"))
                    .build();
            
            MarketingLead savedLead = marketingLeadRepository.save(lead);
            log.info("Successfully saved lead with ID: {}", savedLead.getId());
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            log.error("Failed to save lead: ", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
