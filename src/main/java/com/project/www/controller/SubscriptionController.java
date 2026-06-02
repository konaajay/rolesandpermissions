package com.project.www.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.dto.SubscriptionRequest;
import com.project.www.dto.SubscriptionResponse;
import com.project.www.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUBSCRIPTION_MANAGE')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradeSubscription(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            subscriptionService.upgradeSubscription(request)
        ));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUBSCRIPTION_MANAGE')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionHistory() {
        return ResponseEntity.ok(ApiResponse.success(
            subscriptionService.getSubscriptionHistory()
        ));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getAllSubscriptions() {
        return ResponseEntity.ok(ApiResponse.success(
            subscriptionService.getAllSubscriptions()
        ));
    }
}
