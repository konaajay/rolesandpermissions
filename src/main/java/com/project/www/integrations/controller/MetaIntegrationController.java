package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.MetaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations/meta")
@RequiredArgsConstructor
public class MetaIntegrationController {

    private final MetaService metaService;
    private final IntegrationLogService logService;

    @PostMapping("/configure")
    public ResponseEntity<ApiResponseDto<Void>> configure(@Valid @RequestBody MetaConfigureRequest request) {
        metaService.configure(request);
        return ResponseEntity.ok(ApiResponseDto.success("Meta configured successfully", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponseDto.success(metaService.getStatus()));
    }

    @GetMapping("/pages")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> pages() {
        return ResponseEntity.ok(ApiResponseDto.success(metaService.getPages()));
    }

    @GetMapping("/forms")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> forms() {
        return ResponseEntity.ok(ApiResponseDto.success(metaService.getForms()));
    }

    @PostMapping("/sync-leads")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> syncLeads() {
        return ResponseEntity.ok(ApiResponseDto.success(metaService.syncLeads()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponseDto<Void>> webhook(@RequestBody String payload,
                                                        @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        metaService.handleWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponseDto.success("Webhook received", null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponseDto<?>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                logService.getLogs("META", PageRequest.of(page, size))));
    }
}
