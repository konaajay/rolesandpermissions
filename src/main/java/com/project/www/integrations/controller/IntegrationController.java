package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.service.IntegrationService;

import java.util.List;

/**
 * Core integration APIs - TEMPORARY SUPER ADMIN MODE (no JWT/permissions required).
 * TODO: Add @PreAuthorize when security module is enabled.
 */
@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<IntegrationCardResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponseDto.success(integrationService.getAllIntegrations()));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponseDto<IntegrationDetailsResponse>> getDetails(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponseDto.success(integrationService.getIntegrationDetails(code)));
    }

    @PatchMapping("/{code}/toggle")
    public ResponseEntity<ApiResponseDto<IntegrationDetailsResponse>> toggle(
            @PathVariable String code, @Valid @RequestBody IntegrationToggleRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success("Integration updated successfully",
                integrationService.toggleIntegration(code, request)));
    }

    @PostMapping("/{code}/configure")
    public ResponseEntity<ApiResponseDto<IntegrationDetailsResponse>> configure(
            @PathVariable String code, @Valid @RequestBody IntegrationConfigureRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success("Integration configured successfully",
                integrationService.configureIntegration(code, request)));
    }

    @PostMapping("/{code}/test")
    public ResponseEntity<ApiResponseDto<IntegrationTestResponse>> test(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponseDto.success("Connection test successful",
                integrationService.testIntegration(code)));
    }

    @PostMapping("/{code}/disconnect")
    public ResponseEntity<ApiResponseDto<Void>> disconnect(@PathVariable String code) {
        integrationService.disconnectIntegration(code);
        return ResponseEntity.ok(ApiResponseDto.success("Integration disconnected", null));
    }

    @GetMapping("/{code}/logs")
    public ResponseEntity<ApiResponseDto<Page<IntegrationLogResponse>>> logs(
            @PathVariable String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                integrationService.getIntegrationLogs(code, PageRequest.of(page, size))));
    }

    @GetMapping("/{code}/sync-history")
    public ResponseEntity<ApiResponseDto<List<SyncHistoryResponse>>> syncHistory(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponseDto.success(integrationService.getSyncHistory(code)));
    }

    @GetMapping("/{code}/oauth/connect")
    public ResponseEntity<ApiResponseDto<OAuthConnectResponse>> oauthConnect(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponseDto.success(integrationService.getOAuthConnectUrl(code)));
    }

    @GetMapping("/{code}/oauth/callback")
    public ResponseEntity<Void> oauthCallback(
            @PathVariable("code") String integrationCode,
            @RequestParam(name = "code", required = false) String authorizationCode,
            @RequestParam(required = false) String state) {
        String redirectUrl = integrationService.handleOAuthCallback(integrationCode, authorizationCode, state);
        return ResponseEntity.status(302).header("Location", redirectUrl).build();
    }
}
