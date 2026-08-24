package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.WhatsappService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/integrations/whatsapp")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority(T(com.project.www.integrations.config.IntegrationPermissions).INTEGRATION_VIEW)")
public class WhatsappIntegrationController {

    private final WhatsappService whatsappService;
    private final IntegrationLogService logService;

    @PostMapping("/configure")
    public ResponseEntity<ApiResponseDto<Void>> configure(@Valid @RequestBody WhatsappConfigureRequest request) {
        whatsappService.configure(request);
        return ResponseEntity.ok(ApiResponseDto.success("WhatsApp configured", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponseDto.success(whatsappService.getStatus()));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponseDto<Void>> send(@Valid @RequestBody WhatsappSendRequest request) {
        whatsappService.sendMessage(request);
        return ResponseEntity.ok(ApiResponseDto.success("Message sent", null));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponseDto<IntegrationTestResponse>> test() {
        return ResponseEntity.ok(ApiResponseDto.success(whatsappService.testConnection()));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponseDto<Void>> disconnect() {
        whatsappService.disconnect();
        return ResponseEntity.ok(ApiResponseDto.success("Disconnected", null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponseDto<?>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                logService.getLogs("WHATSAPP", PageRequest.of(page, size))));
    }
}
