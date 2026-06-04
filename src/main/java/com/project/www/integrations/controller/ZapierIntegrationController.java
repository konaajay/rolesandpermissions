package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.dto.ZapierWebhookConfigureRequest;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.ZapierService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/integrations/zapier")
@RequiredArgsConstructor
public class ZapierIntegrationController {

    private final ZapierService zapierService;
    private final IntegrationLogService logService;

    @PostMapping("/api-key/generate")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> generateKey() {
        return ResponseEntity.ok(ApiResponseDto.success(zapierService.generateApiKey()));
    }

    @PostMapping("/api-key/regenerate")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> regenerateKey() {
        return ResponseEntity.ok(ApiResponseDto.success(zapierService.regenerateApiKey()));
    }

    @PostMapping("/api-key/revoke")
    public ResponseEntity<ApiResponseDto<Void>> revokeKey() {
        zapierService.revokeApiKey();
        return ResponseEntity.ok(ApiResponseDto.success("API key revoked", null));
    }

    @GetMapping("/triggers")
    public ResponseEntity<ApiResponseDto<List<String>>> triggers() {
        return ResponseEntity.ok(ApiResponseDto.success(zapierService.getTriggers()));
    }

    @GetMapping("/sample/lead")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> sampleLead() {
        return ResponseEntity.ok(ApiResponseDto.success(zapierService.getSampleLead()));
    }

    @PostMapping("/configure-webhook")
    public ResponseEntity<ApiResponseDto<Void>> configureWebhook(@Valid @RequestBody ZapierWebhookConfigureRequest request) {
        zapierService.configureWebhook(request);
        return ResponseEntity.ok(ApiResponseDto.success("Zapier webhook configured", null));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponseDto<Void>> testZapier() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("leadId", 999);
        payload.put("leadName", "Backend Test Lead");
        payload.put("phone", "9999999999");
        payload.put("email", "backendtest@gmail.com");
        payload.put("source", "Backend Test");
        payload.put("createdAt", java.time.LocalDateTime.now().toString());

        boolean sent = zapierService.sendEvent("LEAD_CREATED", payload);
        if (sent) {
            return ResponseEntity.ok(ApiResponseDto.success("Zapier test event sent", null));
        } else {
            return ResponseEntity.ok(ApiResponseDto.failure("Zapier test event failed", "Failed to send event"));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponseDto<?>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                logService.getLogs("ZAPIER", PageRequest.of(page, size))));
    }
}
