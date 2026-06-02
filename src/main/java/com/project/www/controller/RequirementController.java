package com.project.www.controller;

import com.project.www.dto.RequirementRequest;
import com.project.www.entity.Requirement;
import com.project.www.service.RequirementService;
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
    public ResponseEntity<Requirement> createRequirement(@RequestBody RequirementRequest request) {
        return ResponseEntity.ok(requirementService.createRequirement(request));
    }

    @GetMapping
    public ResponseEntity<List<Requirement>> getAllRequirements() {
        return ResponseEntity.ok(requirementService.getAllRequirements());
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Requirement>> getRequirementsByVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(requirementService.getRequirementsByVendor(vendorId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Requirement> updateRequirementStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(requirementService.updateRequirementStatus(id, status));
    }
}
