package com.project.www.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.dto.VendorInvoiceDto;
import com.project.www.service.VendorInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-invoices")
@RequiredArgsConstructor
public class VendorInvoiceController {

    private final VendorInvoiceService service;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR_INVOICE_CREATE')")
    public ResponseEntity<ApiResponse<VendorInvoiceDto>> createInvoice(@Valid @RequestBody VendorInvoiceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createInvoice(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_INVOICE_UPDATE')")
    public ResponseEntity<ApiResponse<VendorInvoiceDto>> updateInvoice(
            @PathVariable Long id, @Valid @RequestBody VendorInvoiceDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updateInvoice(id, dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_INVOICE_VIEW')")
    public ResponseEntity<ApiResponse<VendorInvoiceDto>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getInvoiceById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_INVOICE_VIEW')")
    public ResponseEntity<ApiResponse<List<VendorInvoiceDto>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllInvoices()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_INVOICE_DELETE')")
    public ResponseEntity<ApiResponse<String>> deleteInvoice(@PathVariable Long id) {
        service.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully"));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAuthority('VENDOR_INVOICE_VIEW')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadReceipt(@PathVariable Long id) {
        VendorInvoiceDto invoice = service.getInvoiceById(id);
        if (invoice.getReceiptUrl() == null || invoice.getReceiptUrl().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(invoice.getReceiptUrl());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (java.net.MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
