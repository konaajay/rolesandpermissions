package com.project.www.integrations.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.project.www.marketing.entity.MarketingLead;
import com.project.www.marketing.repository.MarketingLeadRepository;
import com.project.www.util.TenantContext;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/public/leads")
@RequiredArgsConstructor
public class PublicLeadController {

    private final MarketingLeadRepository leadRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLead(@RequestBody Map<String, Object> payload) {
        // tenant context is already set by ApiKeyAuthInterceptor
        MarketingLead lead = new MarketingLead();
        
        String firstName = (String) payload.getOrDefault("firstName", "");
        String lastName = (String) payload.getOrDefault("lastName", "");
        String name = (String) payload.getOrDefault("name", (firstName + " " + lastName).trim());
        if (name.isEmpty()) {
            name = "Unknown";
        }
        
        lead.setName(name);
        lead.setEmail((String) payload.getOrDefault("email", ""));
        lead.setPhone((String) payload.getOrDefault("phone", ""));
        lead.setSource("API");
        
        leadRepository.save(lead);
        
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
