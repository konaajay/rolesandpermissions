package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorAuditDto;
import com.project.www.vendor.service.VendorAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor-audits")
@RequiredArgsConstructor
public class VendorAuditController {

    private final VendorAuditService service;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR_AUDIT_CREATE')")
    public ResponseEntity<ApiResponse<VendorAuditDto>> createAudit(@Valid @RequestBody VendorAuditDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createAudit(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_AUDIT_UPDATE')")
    public ResponseEntity<ApiResponse<VendorAuditDto>> updateAudit(
            @PathVariable Long id, @Valid @RequestBody VendorAuditDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updateAudit(id, dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_AUDIT_VIEW')")
    public ResponseEntity<ApiResponse<VendorAuditDto>> getAuditById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getAuditById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_AUDIT_VIEW')")
    public ResponseEntity<ApiResponse<List<VendorAuditDto>>> getAllAudits() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllAudits()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_AUDIT_DELETE')")
    public ResponseEntity<ApiResponse<String>> deleteAudit(@PathVariable Long id) {
        service.deleteAudit(id);
        return ResponseEntity.ok(ApiResponse.success("Audit deleted successfully"));
    }
}
