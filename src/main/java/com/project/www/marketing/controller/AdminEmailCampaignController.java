package com.project.www.marketing.controller;

import com.project.www.marketing.entity.EmailCampaign;
import com.project.www.marketing.repository.EmailCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/marketing/campaigns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_VIEW')")
public class AdminEmailCampaignController {

    private final EmailCampaignRepository emailCampaignRepository;

    @GetMapping("/all")
    public ResponseEntity<List<EmailCampaign>> getAllCampaigns() {
        return ResponseEntity.ok(emailCampaignRepository.findAll());
    }

    @GetMapping("/leads")
    public ResponseEntity<List<Object>> getLeads() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_CREATE')")
    public ResponseEntity<EmailCampaign> createCampaign(@RequestBody EmailCampaign request) {
        request.setChannel("EMAIL");
        return ResponseEntity.ok(emailCampaignRepository.save(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_UPDATE')")
    public ResponseEntity<EmailCampaign> updateCampaign(@PathVariable Long id, @RequestBody EmailCampaign request) {
        return emailCampaignRepository.findById(id).map(campaign -> {
            campaign.setTitle(request.getTitle());
            campaign.setSubject(request.getSubject());
            campaign.setContent(request.getContent());
            campaign.setStatus(request.getStatus());
            return ResponseEntity.ok(emailCampaignRepository.save(campaign));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_DELETE')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        if (emailCampaignRepository.existsById(id)) {
            emailCampaignRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_CREATE')")
    public ResponseEntity<EmailCampaign> scheduleCampaign(@PathVariable Long id) {
        return emailCampaignRepository.findById(id).map(campaign -> {
            campaign.setStatus(com.project.www.enums.EmailCampaignStatus.SCHEDULED);
            return ResponseEntity.ok(emailCampaignRepository.save(campaign));
        }).orElse(ResponseEntity.notFound().build());
    }
}
