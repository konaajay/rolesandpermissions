package com.project.www.controller;

import com.project.www.dto.CreateTenantRequest;
import com.project.www.dto.TenantResponse;
import com.project.www.entity.TenantModule;
import com.project.www.service.TenantService;
import com.project.www.service.TenantModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final TenantModuleService tenantModuleService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_CREATE)")
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_VIEW)")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_ENABLE)")
    public ResponseEntity<String> enableTenant(@PathVariable Long id) {
        tenantService.enableTenant(id);
        return ResponseEntity.ok("Tenant Enabled Successfully");
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_DISABLE)")
    public ResponseEntity<String> disableTenant(@PathVariable Long id) {
        tenantService.disableTenant(id);
        return ResponseEntity.ok("Tenant Disabled Successfully");
    }

    @GetMapping("/{id}/modules")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_VIEW)")
    public ResponseEntity<List<TenantModule>> getTenantModules(@PathVariable Long id) {
        return ResponseEntity.ok(tenantModuleService.getModulesForTenant(id));
    }

    @PutMapping("/{id}/modules/{moduleName}/enable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_ENABLE)")
    public ResponseEntity<String> enableTenantModule(
            @PathVariable Long id, 
            @PathVariable String moduleName,
            @RequestBody(required = false) com.project.www.dto.TenantModuleUpdateRequest request) {
        tenantModuleService.enableModule(id, moduleName, request);
        return ResponseEntity.ok("Module Enabled/Updated Successfully");
    }

    @PutMapping("/{id}/modules/{moduleName}/disable")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_DISABLE)")
    public ResponseEntity<String> disableTenantModule(@PathVariable Long id, @PathVariable String moduleName) {
        tenantModuleService.disableModule(id, moduleName);
        return ResponseEntity.ok("Module Disabled Successfully");
    }
}
