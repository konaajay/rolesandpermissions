package com.project.www.controller;

import com.project.www.entity.TenantSettings;
import com.project.www.repository.TenantSettingsRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import com.project.www.dto.SettingsDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class TenantSettingsController {

    private final TenantSettingsRepository tenantSettingsRepository;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public SettingsDto getSettings() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context");
        }
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElse(TenantSettings.builder().tenantId(tenantId).employeeSequence(0L).leadSequence(0L).build());

        SettingsDto dto = new SettingsDto();
        dto.setEmployeeIdFormat(settings.getEmployeeIdFormat());
        dto.setLeadIdFormat(settings.getLeadIdFormat());
        dto.setEmployeeSequence(settings.getEmployeeSequence());
        dto.setLeadSequence(settings.getLeadSequence());
        return dto;
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public SettingsDto updateSettings(@RequestBody SettingsDto request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context");
        }
        
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId)
                .orElse(TenantSettings.builder().tenantId(tenantId).employeeSequence(0L).leadSequence(0L).build());
                
        settings.setEmployeeIdFormat(request.getEmployeeIdFormat());
        settings.setLeadIdFormat(request.getLeadIdFormat());
        
        if (request.getEmployeeSequence() != null) {
            settings.setEmployeeSequence(request.getEmployeeSequence());
        }
        if (request.getLeadSequence() != null) {
            settings.setLeadSequence(request.getLeadSequence());
        }
        
        tenantSettingsRepository.save(settings);

        SettingsDto dto = new SettingsDto();
        dto.setEmployeeIdFormat(settings.getEmployeeIdFormat());
        dto.setLeadIdFormat(settings.getLeadIdFormat());
        dto.setEmployeeSequence(settings.getEmployeeSequence());
        dto.setLeadSequence(settings.getLeadSequence());
        return dto;
    }
}
