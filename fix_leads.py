import os

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/controller/PublicLeadController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = '''package com.project.www.integrations.controller;

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
        Long tenantId = TenantContext.getCurrentTenant();
        
        MarketingLead lead = new MarketingLead();
        lead.setFirstName((String) payload.getOrDefault("firstName", "Unknown"));
        lead.setLastName((String) payload.getOrDefault("lastName", ""));
        lead.setEmail((String) payload.getOrDefault("email", ""));
        lead.setPhone((String) payload.getOrDefault("phone", ""));
        lead.setCompany((String) payload.getOrDefault("company", ""));
        lead.setSource("API");
        lead.setStatus("NEW");
        lead.setTenantId(tenantId);
        
        leadRepository.save(lead);
        
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
'''

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed PublicLeadController')
