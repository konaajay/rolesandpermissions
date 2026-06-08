package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorPerformanceDto;
import com.project.www.vendor.service.VendorPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor-performance")
@RequiredArgsConstructor
public class VendorPerformanceController {

    private final VendorPerformanceService service;

    @GetMapping
    @PreAuthorize("hasAuthority('PERFORMANCE_VIEW')")
    public ResponseEntity<ApiResponse<VendorPerformanceDto>> getPerformanceData() {
        return ResponseEntity.ok(ApiResponse.success(service.getPerformanceData()));
    }
}
