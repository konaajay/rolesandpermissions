package com.project.www.vendor.controller;

import com.project.www.vendor.dto.RequirementRequest;
import com.project.www.vendor.dto.RequirementResponseDto;
import com.project.www.vendor.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @PostMapping
    public ResponseEntity<RequirementResponseDto> createRequirement(@RequestBody RequirementRequest request) {
        return ResponseEntity.ok(requirementService.createRequirement(request));
    }

    @GetMapping
    public ResponseEntity<List<RequirementResponseDto>> getAllRequirements() {
        return ResponseEntity.ok(requirementService.getAllRequirements());
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<RequirementResponseDto>> getRequirementsByVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(requirementService.getRequirementsByVendor(vendorId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RequirementResponseDto> updateRequirementStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(requirementService.updateRequirementStatus(id, status));
    }
}
