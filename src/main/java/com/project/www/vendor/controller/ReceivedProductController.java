package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.ProductAssignmentDto;
import com.project.www.vendor.dto.ProductAssignmentRequest;
import com.project.www.vendor.dto.ReceivedProductDto;
import com.project.www.vendor.service.ReceivedProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendors/received-products")
@RequiredArgsConstructor
public class ReceivedProductController {

    private final ReceivedProductService receivedProductService;

    @PostMapping("/receive/{requirementItemId}")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ReceivedProductDto>> receiveRequirementItem(
            @PathVariable("requirementItemId") Long requirementItemId, 
            @RequestParam("quantity") Integer quantity) {
        ReceivedProductDto created = receivedProductService.receiveRequirementItem(requirementItemId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<Page<ReceivedProductDto>>> getReceivedProducts(Pageable pageable) {
        Page<ReceivedProductDto> products = receivedProductService.getReceivedProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/requirement/{requirementId}")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<List<ReceivedProductDto>>> getReceivedProductsByRequirement(@PathVariable("requirementId") Long requirementId) {
        List<ReceivedProductDto> products = receivedProductService.getReceivedProductsByRequirementId(requirementId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> assignProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductAssignmentRequest request) {
        ProductAssignmentDto assignment = receivedProductService.assignProduct(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(assignment));
    }


    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ReceivedProductDto>> returnProduct(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity) {
        ReceivedProductDto returned = receivedProductService.returnProduct(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(returned));
    }

    @PostMapping("/assignments/{assignmentId}/damage")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> reportDamage(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.reportDamage(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/repair")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> sendForRepair(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.sendForRepair(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/complete-repair")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> completeRepair(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.completeRepair(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/consume")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> markConsumed(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.markConsumed(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/not-repairable")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> markNotRepairable(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.markNotRepairable(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/return")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> returnAssignment(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.returnAssignment(assignmentId, request.getDescription())));
    }

    @GetMapping("/assignments/{assignmentId}/history")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<java.util.List<com.project.www.vendor.dto.ProductLifecycleEventDto>>> getAssignmentHistory(
            @PathVariable("assignmentId") Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.getAssignmentHistory(assignmentId)));
    }


    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAllAssignments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.getAllAssignments(pageable)));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    

    public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAssignments(
            @PathVariable("id") Long id,
            Pageable pageable) {
        Page<ProductAssignmentDto> assignments = receivedProductService.getAssignmentsForProduct(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
}
