package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.service.CashfreeService;
import com.project.www.integrations.service.IntegrationLogService;

import java.util.Map;

@RestController
@RequestMapping("/integrations/cashfree")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority(T(com.project.www.integrations.config.IntegrationPermissions).PAYMENT_CONFIGURE)")
public class CashfreeIntegrationController {

    private final CashfreeService cashfreeService;
    private final IntegrationLogService logService;

    @PostMapping("/configure")
    public ResponseEntity<ApiResponseDto<Void>> configure(@Valid @RequestBody CashfreeConfigureRequest request) {
        cashfreeService.configure(request);
        return ResponseEntity.ok(ApiResponseDto.success("Cashfree configured", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponseDto.success(cashfreeService.getStatus()));
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> createOrder(
            @Valid @RequestBody CashfreeCreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(cashfreeService.createOrder(request)));
    }

    @PostMapping("/create-payment-link")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> createPaymentLink(
            @Valid @RequestBody CashfreePaymentLinkRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(cashfreeService.createPaymentLink(request)));
    }

    @GetMapping("/payment-status/{orderId}")
    public ResponseEntity<ApiResponseDto<CashfreePaymentStatusResponse>> paymentStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponseDto.success(cashfreeService.getPaymentStatus(orderId)));
    }

    @PostMapping("/webhook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponseDto<Void>> webhook(@RequestBody String payload) {
        cashfreeService.handleWebhook(payload);
        return ResponseEntity.ok(ApiResponseDto.success("Webhook processed", null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponseDto<?>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                logService.getLogs("CASHFREE", PageRequest.of(page, size))));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponseDto<Void>> disconnect() {
        cashfreeService.disconnect();
        return ResponseEntity.ok(ApiResponseDto.success("Disconnected", null));
    }
}
