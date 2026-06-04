package com.project.www.controller;

import com.project.www.enums.*;

import com.project.www.entity.Content;
import com.project.www.service.ContentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/marketing/content")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContentController {

    private final ContentService contentService;

    // =========================
    // CREATE
    // =========================
    @PreAuthorize("hasAuthority('MARKETING_CONTENT_CREATE')")
    @PostMapping
    public ResponseEntity<?> createContent(@Valid @RequestBody Content content) {
        return ResponseEntity.ok(contentService.createContent(content));
    }

    // =========================
    // READ
    // =========================
    @PreAuthorize("hasAuthority('MARKETING_CONTENT_VIEW')")
    @GetMapping
    public ResponseEntity<?> getContentByCampaign(@RequestParam Long campaignId) {
        return ResponseEntity.ok(contentService.findByCampaign(campaignId));
    }

    // =========================
    // UPDATE
    // =========================
    @PreAuthorize("hasAuthority('MARKETING_CONTENT_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContent(@PathVariable Long id,
            @Valid @RequestBody Content updated) {
        return ResponseEntity.ok(contentService.updateContent(id, updated));
    }

    // =========================
    // DELETE
    // =========================
    @PreAuthorize("hasAuthority('MARKETING_CONTENT_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.ok().body("{\"message\":\"Content deleted\"}");
    }
}
