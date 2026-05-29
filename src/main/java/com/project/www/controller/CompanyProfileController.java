package com.project.www.controller;

import com.project.www.dto.CompanyProfileDTO;
import com.project.www.service.CompanyProfileService;
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
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_VIEW)")
    public ResponseEntity<CompanyProfileDTO> getCompanyProfile() {
        CompanyProfileDTO dto = service.getCompanyProfile();
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public ResponseEntity<CompanyProfileDTO> updateCompanyProfile(@RequestBody CompanyProfileDTO req) {
        return ResponseEntity.ok(service.updateCompanyProfile(req));
    }

    @PostMapping("/logo")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public ResponseEntity<CompanyProfileDTO> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadLogo(file));
    }

    @PostMapping("/favicon")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public ResponseEntity<CompanyProfileDTO> uploadFavicon(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadFavicon(file));
    }
}
