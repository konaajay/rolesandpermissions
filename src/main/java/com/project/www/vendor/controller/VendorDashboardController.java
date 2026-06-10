package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorDashboardDto;
import com.project.www.vendor.service.VendorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/vendor-dashboard", "/vendor-dashboard"})
@RequiredArgsConstructor
public class VendorDashboardController {

    private final VendorDashboardService service;

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<VendorDashboardDto>> getDashboardData(
            @RequestParam(value = "filter", defaultValue = "Last 6 months") String filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getDashboardData(filter)));
    }
}
