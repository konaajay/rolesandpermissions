package com.project.www.tenant.controller;

import com.project.www.tenant.entity.IdFormatSetting;
import com.project.www.tenant.repository.IdFormatSettingRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/id-formats")
@RequiredArgsConstructor
public class IdFormatSettingController {

    private final IdFormatSettingRepository idFormatSettingRepository;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public ResponseEntity<List<IdFormatSetting>> getAllFormats() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(idFormatSettingRepository.findByTenantId(tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public ResponseEntity<IdFormatSetting> saveFormat(@RequestBody IdFormatSetting request) {
        Long tenantId = TenantContext.getCurrentTenant();
        IdFormatSetting setting = idFormatSettingRepository.findByTenantIdAndEntityType(tenantId, request.getEntityType())
                .orElse(IdFormatSetting.builder().tenantId(tenantId).entityType(request.getEntityType()).build());
        
        setting.setPrefix(request.getPrefix());
        setting.setPaddingLength(request.getPaddingLength() != null ? request.getPaddingLength() : 7);
        setting.setNextSequence(request.getNextSequence() != null ? request.getNextSequence() : 1L);
        setting.setActive(request.getActive() != null ? request.getActive() : true);
        
        return ResponseEntity.ok(idFormatSettingRepository.save(setting));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public ResponseEntity<Void> deleteFormat(@PathVariable Long id) {
        idFormatSettingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
