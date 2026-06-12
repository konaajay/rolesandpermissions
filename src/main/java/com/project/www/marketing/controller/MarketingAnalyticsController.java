package com.project.www.marketing.controller;

import com.project.www.marketing.entity.TrafficEvent;
import com.project.www.marketing.service.MarketingAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/marketing/analytics")
@RequiredArgsConstructor
public class MarketingAnalyticsController {

    private final MarketingAnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @PostMapping("/public/track")
    public ResponseEntity<Map<String, Object>> trackEvent(@RequestBody Map<String, Object> data) {
        TrafficEvent event = new TrafficEvent();
        event.setSessionId((String) data.get("sessionId"));
        event.setEventType((String) data.get("eventType"));
        event.setUtmSource((String) data.get("utmSource"));
        event.setUtmMedium((String) data.get("utmMedium"));
        event.setUtmCampaign((String) data.get("utmCampaign"));
        event.setSource((String) data.get("source"));
        event.setTrackedLinkId((String) data.get("page"));

        analyticsService.recordEvent(event);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
