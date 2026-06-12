package com.project.www.marketing.controller;

import com.project.www.marketing.entity.EmailCampaign;
import com.project.www.marketing.entity.EmailRecipient;
import com.project.www.marketing.repository.EmailCampaignRepository;
import com.project.www.service.EmailService;
import com.project.www.enums.EmailCampaignStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/marketing/campaigns")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_VIEW')")
public class AdminEmailCampaignController {

    private final EmailCampaignRepository emailCampaignRepository;
    private final EmailService emailService;

    @GetMapping("/all")
    public ResponseEntity<List<EmailCampaign>> getAllCampaigns() {
        return ResponseEntity.ok(emailCampaignRepository.findAll());
    }

    @GetMapping("/leads")
    public ResponseEntity<List<Object>> getLeads() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    public static class CreateCampaignRequest {
        public String title;
        public String subject;
        public String content;
        public EmailCampaignStatus status;
        public List<String> recipients;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_CREATE')")
    public ResponseEntity<EmailCampaign> createCampaign(@RequestBody CreateCampaignRequest request) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setChannel("EMAIL");
        campaign.setTitle(request.title);
        campaign.setSubject(request.subject);
        campaign.setContent(request.content);
        campaign.setStatus(request.status);
        
        List<EmailRecipient> recipients = new ArrayList<>();
        if (request.recipients != null) {
            for (String email : request.recipients) {
                EmailRecipient r = new EmailRecipient();
                r.setEmail(email);
                r.setCampaign(campaign);
                recipients.add(r);
            }
        }
        campaign.setRecipients(recipients);
        campaign.setTotalRecipients(recipients.size());
        
        int success = 0;
        int failed = 0;
        
        if (request.status == EmailCampaignStatus.PENDING) {
            for (EmailRecipient r : recipients) {
                try {
                    emailService.sendEmail(r.getEmail(), campaign.getSubject(), campaign.getContent());
                    r.setStatus(com.project.www.enums.EmailStatus.SENT);
                    success++;
                } catch (Exception e) {
                    r.setStatus(com.project.www.enums.EmailStatus.FAILED);
                    r.setFailureReason(e.getMessage());
                    failed++;
                    log.error("Failed to send email to {}: {}", r.getEmail(), e.getMessage());
                }
            }
            if (success > 0) {
                campaign.setStatus(EmailCampaignStatus.COMPLETED);
            } else if (failed > 0 && success == 0) {
                campaign.setStatus(EmailCampaignStatus.FAILED);
            }
        }
        
        campaign.setSuccessCount(success);
        campaign.setFailedCount(failed);

        return ResponseEntity.ok(emailCampaignRepository.save(campaign));
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
