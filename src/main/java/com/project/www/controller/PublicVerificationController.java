package com.project.www.controller;

import com.project.www.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/verify")
public class PublicVerificationController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private com.project.www.repository.TenantRepository tenantRepository;

    @GetMapping("/{identifier}")
    public ResponseEntity<?> verifyCertificate(@PathVariable String identifier) {
        // Since verification tokens do not encode the tenant ID and public endpoints 
        // lack a JWT, we must search across all active tenant databases.
        // Clear any potentially lingering thread-local context before accessing master DB
        com.project.www.util.TenantContext.clear();
        java.util.List<com.project.www.entity.Tenant> tenants = tenantRepository.findAll();
        for (com.project.www.entity.Tenant tenant : tenants) {
            if (Boolean.TRUE.equals(tenant.getActive())) {
                try {
                    com.project.www.util.TenantContext.setCurrentTenant(tenant.getId());
                    com.project.www.util.TenantContext.setCurrentTenantCode(tenant.getCode());
                    
                    Object dto = certificateService.verifyCertificate(identifier);
                    return ResponseEntity.ok(dto);
                } catch (Exception e) {
                    // Not found in this tenant, continue to next
                } finally {
                    com.project.www.util.TenantContext.clear();
                }
            }
        }
        
        return ResponseEntity.badRequest().body("Certificate Not Found or Invalid");
    }
}
