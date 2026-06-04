package com.project.www.integrations.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.event.IntegrationEventPublisher;

import java.util.Map;

@RestController
@RequestMapping("/api/test-events")
@RequiredArgsConstructor
public class TestEventController {

    private final IntegrationEventPublisher integrationEventPublisher;

    @PostMapping("/lead-created")
    public ResponseEntity<ApiResponseDto<Void>> testLeadCreated() {
        integrationEventPublisher.publish(
                "lead.created",
                Map.of(
                        "leadId", 101,
                        "name", "Test Lead",
                        "phone", "9876543210",
                        "email", "testlead@gmail.com",
                        "source", "Website"
                ),
                "CRM",
                101L
        );
        return ResponseEntity.ok(ApiResponseDto.success("Lead created event published", null));
    }

    @PostMapping("/payment-success")
    public ResponseEntity<ApiResponseDto<Void>> testPaymentSuccess() {
        integrationEventPublisher.publish(
                "payment.success",
                Map.of(
                        "orderId", "ORDER_1001",
                        "paymentId", "PAY_1001",
                        "amount", 999,
                        "status", "SUCCESS",
                        "provider", "CASHFREE"
                ),
                "PAYMENT",
                1001L
        );
        return ResponseEntity.ok(ApiResponseDto.success("Payment success event published", null));
    }
    @PostMapping("/unsupported")
    public ResponseEntity<ApiResponseDto<Void>> testUnsupportedEvent() {

        integrationEventPublisher.publish(
                "invoice.created",
                Map.of(
                        "invoiceId", 501,
                        "amount", 1500,
                        "status", "CREATED"
                ),
                "BILLING",
                501L
        );

        return ResponseEntity.ok(ApiResponseDto.success("Unsupported event published", null));
    }
}

