package com.project.www.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.dto.PurchaseOrderDto;
import com.project.www.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @PostMapping
    @PreAuthorize("hasAuthority('PO_CREATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createPO(@Valid @RequestBody PurchaseOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createPO(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PO_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> updatePO(
            @PathVariable Long id, @Valid @RequestBody PurchaseOrderDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updatePO(id, dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PO_VIEW')")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getPOById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getPOById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PO_VIEW')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto>>> getAllPOs() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPOs()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PO_DELETE')")
    public ResponseEntity<ApiResponse<String>> deletePO(@PathVariable Long id) {
        service.deletePO(id);
        return ResponseEntity.ok(ApiResponse.success("PO deleted successfully"));
    }
}
