package com.project.www.controller;

import com.project.www.enums.*;

import com.project.www.entity.Campaign;
import com.project.www.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/marketing/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final CampaignService campaignService;
    private final com.project.www.service.CsvImportService csvImportService;

    // Specific static paths first to avoid ambiguity with /{id}
    @GetMapping("/all")
    
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaignEntities());
    }

    @GetMapping("/leads")
    
    public ResponseEntity<?> getAllLeads() {
        return ResponseEntity.ok(campaignService.getAllLeads());
    }

    @GetMapping("/summary")
    
    public ResponseEntity<?> getSummary() {
        return ResponseEntity.ok(campaignService.getMarketingSummary());
    }

    // Dynamic paths last
    @GetMapping("/{id}")
    
    public ResponseEntity<Campaign> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    @PostMapping
    public ResponseEntity<?> createCampaign(@RequestBody com.project.www.dto.CampaignRequestDTO request) {
        return ResponseEntity.status(201).body(campaignService.processCampaign(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(@PathVariable Long id, @RequestBody com.project.www.dto.CampaignRequestDTO request) {
        return ResponseEntity.ok(campaignService.processCampaignUpdate(id, request));
    }

    @DeleteMapping("/{id}")
    
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/import-csv")
    
    public ResponseEntity<?> importCsv(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            return ResponseEntity.ok(csvImportService.importLeadsFromCsv(file, "CAMPAIGN_" + id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Import failed: " + e.getMessage());
        }
    }
}
