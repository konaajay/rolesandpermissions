package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorComplaintDto;
import com.project.www.vendor.service.impl.VendorComplaintServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor-complaints")
@RequiredArgsConstructor
public class VendorComplaintController {

    private final VendorComplaintServiceImpl service;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('VENDOR_CREATE')")
    public ResponseEntity<?> create(@RequestBody VendorComplaintDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody VendorComplaintDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN') or hasAuthority('VENDOR_DELETE')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted"));
    }
}
