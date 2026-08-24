package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.ApiKeyCreateRequest;
import com.project.www.integrations.dto.ApiKeyResponse;
import com.project.www.integrations.dto.ApiKeyUsageLogResponse;
import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.service.ApiKeyService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/integrations/api-keys")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority(T(com.project.www.integrations.config.IntegrationPermissions).INTEGRATION_API_KEY_MANAGE)")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ApiKeyResponse>> create(@Valid @RequestBody ApiKeyCreateRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success("API key created", apiKeyService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ApiKeyResponse>>> list() {
        return ResponseEntity.ok(ApiResponseDto.success(apiKeyService.list()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ApiKeyResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ApiKeyCreateRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(apiKeyService.update(id, request)));
    }

    @PostMapping("/{id}/regenerate")
    public ResponseEntity<ApiResponseDto<ApiKeyResponse>> regenerate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDto.success("API key regenerated", apiKeyService.regenerate(id)));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<ApiResponseDto<Void>> revoke(@PathVariable Long id) {
        apiKeyService.revoke(id);
        return ResponseEntity.ok(ApiResponseDto.success("API key revoked", null));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponseDto<Page<ApiKeyUsageLogResponse>>> logs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(apiKeyService.getUsageLogs(id, PageRequest.of(page, size))));
    }
}
