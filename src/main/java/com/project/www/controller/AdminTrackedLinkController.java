package com.project.www.controller;

import com.project.www.enums.*;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project.www.entity.TrackedLink;
import com.project.www.service.TrackedLinkService;

@RestController
@RequestMapping("/marketing/admin/tracked-links")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminTrackedLinkController {

    private final TrackedLinkService service;

    // ===== CREATE =====
    @PostMapping
    
    public ResponseEntity<TrackedLink> createLink(
            @Valid @RequestBody TrackedLink link) {

        return ResponseEntity.ok(service.saveLink(link));
    }

    // ===== VIEW =====
    @GetMapping
    
    public ResponseEntity<?> getAllLinks() {
        return ResponseEntity.ok(service.getAllLinksWithAnalytics());
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    
    public ResponseEntity<Void> deleteLink(@PathVariable Long id) {
        service.deleteLink(id);
        return ResponseEntity.noContent().build();
    }
}
