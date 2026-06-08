package com.project.www.tenant.controller;

import com.project.www.tenant.dto.OnboardingConfigDTO;
import com.project.www.tenant.service.OnboardingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/onboarding-configs")
@RequiredArgsConstructor
public class OnboardingConfigController {

    private final OnboardingConfigService service;

    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE_ONBOARDING') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<List<OnboardingConfigDTO>> getAllConfigs() {
        return ResponseEntity.ok(service.getAllConfigs());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE_ONBOARDING') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<OnboardingConfigDTO> getConfigByRoleId(@PathVariable Long roleId) {
        OnboardingConfigDTO dto = service.getConfigByRoleId(roleId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE_ONBOARDING') or hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<OnboardingConfigDTO> saveOrUpdateConfig(
            @PathVariable Long roleId,
            @RequestBody OnboardingConfigDTO req) {
        return ResponseEntity.ok(service.saveOrUpdateConfig(roleId, req));
    }
}
