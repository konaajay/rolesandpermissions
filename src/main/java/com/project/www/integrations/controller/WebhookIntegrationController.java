package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.dto.WebhookDeliveryLogResponse;
import com.project.www.integrations.dto.WebhookSubscriptionRequest;
import com.project.www.integrations.dto.WebhookSubscriptionResponse;
import com.project.www.integrations.service.WebhookService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/integrations/webhooks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority(T(com.project.www.integrations.config.IntegrationPermissions).INTEGRATION_WEBHOOK_MANAGE)")
public class WebhookIntegrationController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<WebhookSubscriptionResponse>> create(
            @Valid @RequestBody WebhookSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(webhookService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<WebhookSubscriptionResponse>>> list() {
        return ResponseEntity.ok(ApiResponseDto.success(webhookService.list()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<WebhookSubscriptionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody WebhookSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(webhookService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        webhookService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("Webhook deleted", null));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponseDto<Void>> test(@PathVariable Long id) {
        webhookService.test(id);
        return ResponseEntity.ok(ApiResponseDto.success("Test webhook sent", null));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponseDto<Page<WebhookDeliveryLogResponse>>> logs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                webhookService.getLogs(id, PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/retry/{logId}")
    public ResponseEntity<ApiResponseDto<Void>> retry(@PathVariable Long id, @PathVariable Long logId) {
        webhookService.retry(id, logId);
        return ResponseEntity.ok(ApiResponseDto.success("Retry initiated", null));
    }
}
