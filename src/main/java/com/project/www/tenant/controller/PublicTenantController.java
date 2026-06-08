package com.project.www.tenant.controller;

import com.project.www.tenant.entity.Tenant;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.tenant.service.CompanyProfileService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/public/tenant-branding")
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantRepository tenantRepository;
    private final CompanyProfileService companyProfileService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBrandingByDomain(@RequestParam("domain") String domain) {
        // Strip out port if included (e.g. localhost:5173 -> localhost)
        String cleanDomain = domain.split(":")[0];
        
        Tenant tenant = tenantRepository.findByDomain(cleanDomain).orElse(null);
        if (tenant == null) {
            // Also try matching by subdomain if it ends with a known base domain, 
            // but for exact matches, it will hit the DB.
            return ResponseEntity.notFound().build();
        }

        try {
            TenantContext.setCurrentTenant(tenant.getId());
            TenantContext.setCurrentTenantCode(tenant.getCode());
            
            var profile = companyProfileService.getCompanyProfile();
            
            Map<String, Object> response = new HashMap<>();
            response.put("tenantCode", tenant.getCode());
            response.put("companyName", profile.getCompanyName());
            response.put("logoUrl", profile.getLogoUrl());
            response.put("faviconUrl", profile.getFaviconUrl());
            response.put("headerImageUrl", profile.getHeaderImageUrl());
            
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }
}
