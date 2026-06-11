package com.project.www.marketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/marketing/analytics")
public class MarketingAnalyticsController {

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @PostMapping("/public/track")
    public ResponseEntity<Map<String, Object>> trackEvent(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
