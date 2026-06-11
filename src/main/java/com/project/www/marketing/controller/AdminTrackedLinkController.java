package com.project.www.marketing.controller;

import com.project.www.marketing.entity.TrackedLink;
import com.project.www.marketing.repository.TrackedLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/marketing/admin/tracked-links")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_VIEW')")
public class AdminTrackedLinkController {

    private final TrackedLinkRepository trackedLinkRepository;

    @GetMapping
    public ResponseEntity<List<TrackedLink>> getAllTrackedLinks() {
        return ResponseEntity.ok(trackedLinkRepository.findAllByOrderByTimestampDesc());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_CREATE')")
    public ResponseEntity<TrackedLink> createTrackedLink(@RequestBody TrackedLink request) {
        if (request.getTimestamp() == null) {
            request.setTimestamp(LocalDateTime.now());
        }
        return ResponseEntity.ok(trackedLinkRepository.save(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('MARKETING_DELETE')")
    public ResponseEntity<Void> deleteTrackedLink(@PathVariable Long id) {
        trackedLinkRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
