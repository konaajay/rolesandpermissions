package com.project.www.controller;

import com.project.www.dto.GenerateCertificateDto;
import com.project.www.entity.EmployeeCertificate;
import com.project.www.service.CertificateService;
import com.project.www.util.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping("/generate")
    public ResponseEntity<EmployeeCertificate> generateCertificate(
            @RequestBody GenerateCertificateDto dto) throws Exception {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(certificateService.generateCertificate(tenantId, dto));
    }

    @PostMapping("/preview")
    public ResponseEntity<String> previewCertificate(
            @RequestBody GenerateCertificateDto dto) throws Exception {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(certificateService.previewCertificateHtml(tenantId, dto));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeCertificate>> getAllCertificates() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(certificateService.getAllCertificates(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeCertificate> getCertificate(
            @PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(certificateService.getCertificateById(tenantId, id));
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<EmployeeCertificate> revokeCertificate(
            @PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(certificateService.revokeCertificate(tenantId, id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificatePdf(
            @PathVariable Long id) throws Exception {
        Long tenantId = TenantContext.getCurrentTenant();
        byte[] pdfBytes = certificateService.downloadCertificatePdf(tenantId, id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
