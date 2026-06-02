package com.project.www.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.dto.VendorDto;
import com.project.www.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR_CREATE')")
    public ResponseEntity<ApiResponse<VendorDto>> createVendor(@Valid @RequestBody VendorDto vendorDto) {
        VendorDto createdVendor = vendorService.createVendor(vendorDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdVendor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<VendorDto>> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody VendorDto vendorDto) {
        VendorDto updatedVendor = vendorService.updateVendor(id, vendorDto);
        return ResponseEntity.ok(ApiResponse.success(updatedVendor));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<VendorDto>> getVendorById(@PathVariable Long id) {
        VendorDto vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<Page<VendorDto>>> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Page<VendorDto> vendors = vendorService.getAllVendors(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<Page<VendorDto>>> searchVendors(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Page<VendorDto> vendors = vendorService.searchVendors(searchTerm, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_DELETE')")
    public ResponseEntity<ApiResponse<String>> softDeleteVendor(@PathVariable Long id) {
        vendorService.softDeleteVendor(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<VendorDto>> toggleVendorStatus(@PathVariable Long id) {
        VendorDto vendor = vendorService.toggleVendorStatus(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<VendorDto>> uploadVendorDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        VendorDto vendor = vendorService.uploadVendorDocument(id, file);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }
}
