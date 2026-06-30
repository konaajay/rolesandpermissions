package com.project.www.tenant.controller;

import com.project.www.tenant.dto.CompanyProfileDTO;
import com.project.www.tenant.service.CompanyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/company-profile")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompanyProfileDTO> getCompanyProfile() {
        CompanyProfileDTO dto = service.getCompanyProfile();
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<CompanyProfileDTO> updateCompanyProfile(@RequestBody CompanyProfileDTO req) {
        return ResponseEntity.ok(service.updateCompanyProfile(req));
    }

    @PostMapping("/logo")
    public ResponseEntity<CompanyProfileDTO> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadLogo(file));
    }

    @PostMapping("/favicon")
    public ResponseEntity<CompanyProfileDTO> uploadFavicon(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadFavicon(file));
    }

    @PostMapping("/stamp")
    public ResponseEntity<CompanyProfileDTO> uploadStamp(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadStamp(file));
    }

    @PostMapping("/signature")
    public ResponseEntity<CompanyProfileDTO> uploadSignature(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadSignature(file));
    }
}
