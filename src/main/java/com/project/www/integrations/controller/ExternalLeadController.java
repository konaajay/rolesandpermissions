package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.dto.ExternalLeadCreateRequest;
import com.project.www.integrations.service.TenantContextService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/leads")
@RequiredArgsConstructor
public class ExternalLeadController {

    private final TenantContextService tenantContextService;

    @GetMapping("/test")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> testApiKeyAuth() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "API key authentication working");
        data.put("tenantId", tenantContextService.getCurrentTenantId());
        return ResponseEntity.ok(ApiResponseDto.success("API key authentication working", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> createLead(@Valid @RequestBody ExternalLeadCreateRequest request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", request.getName());
        data.put("phone", request.getPhone());
        data.put("email", request.getEmail());
        data.put("source", request.getSource());
        return ResponseEntity.ok(ApiResponseDto.success("Lead received successfully", data));
    }
}
