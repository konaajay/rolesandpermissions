package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorContractDto;
import com.project.www.vendor.service.VendorContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor-contracts")
@RequiredArgsConstructor
public class VendorContractController {

    private final VendorContractService service;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR_CONTRACT_CREATE')")
    public ResponseEntity<ApiResponse<VendorContractDto>> createContract(@Valid @RequestBody VendorContractDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createContract(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_CONTRACT_UPDATE')")
    public ResponseEntity<ApiResponse<VendorContractDto>> updateContract(
            @PathVariable Long id, @Valid @RequestBody VendorContractDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updateContract(id, dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<VendorContractDto>> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getContractById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_CONTRACT_VIEW')")
    public ResponseEntity<ApiResponse<List<VendorContractDto>>> getAllContracts() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllContracts()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_CONTRACT_DELETE')")
    public ResponseEntity<ApiResponse<String>> deleteContract(@PathVariable Long id) {
        service.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success("Contract deleted successfully"));
    }

    @PostMapping("/{id}/upload")
    @PreAuthorize("hasAuthority('VENDOR_CONTRACT_UPDATE')")
    public ResponseEntity<ApiResponse<VendorContractDto>> uploadContractDocument(
            @PathVariable Long id, 
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile[] files,
            @RequestParam(value = "files", required = false) org.springframework.web.multipart.MultipartFile[] fallbackFiles) {
            
        org.springframework.web.multipart.MultipartFile[] actualFiles = (files != null && files.length > 0) ? files : fallbackFiles;
        
        if (actualFiles == null || actualFiles.length == 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            java.io.File directory = new java.io.File("uploads/contracts");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            VendorContractDto contract = service.getContractById(id);
            java.util.List<String> currentUrls = new java.util.ArrayList<>();
            
            if (contract.getDocumentUrl() != null && !contract.getDocumentUrl().trim().isEmpty()) {
                currentUrls.addAll(java.util.Arrays.asList(contract.getDocumentUrl().split(",")));
            }
            
            for (org.springframework.web.multipart.MultipartFile file : actualFiles) {
                if (file.isEmpty()) continue;
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/contracts", fileName);
                java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                currentUrls.add(filePath.toString().replace("\\", "/"));
            }
            
            contract.setDocumentUrl(String.join(",", currentUrls));
            return ResponseEntity.ok(ApiResponse.success(service.updateContract(id, contract)));
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
