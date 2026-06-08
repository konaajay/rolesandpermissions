package com.project.www.marketing.controller;

import com.project.www.marketing.controller.InteractionController;

import com.project.www.marketing.entity.Interaction;

import com.project.www.enums.*;

import java.util.List;

import com.project.www.marketing.entity.Interaction;
import com.project.www.marketing.service.InteractionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/marketing/interactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InteractionController {

    private final InteractionService interactionService;

    // =========================
    // PUBLIC TRACKING
    // =========================
    @PostMapping
    public ResponseEntity<?> recordInteraction(@Valid @RequestBody Interaction interaction) {
        return ResponseEntity.ok(interactionService.recordInteraction(interaction));
    }

    // =========================
    // ANALYTICS (VIEW)
    // =========================
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_INTERACTION_VIEW')")
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<Interaction>> getByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(interactionService.getByCampaign(campaignId));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_INTERACTION_VIEW')")
    @GetMapping("/customer")
    public ResponseEntity<List<Interaction>> getByCustomer(@RequestParam String email) {
        return ResponseEntity.ok(interactionService.getByCustomer(email));
    }
}
