package com.project.www.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.dto.VendorContractDto;
import com.project.www.service.VendorContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-contracts")
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
}
