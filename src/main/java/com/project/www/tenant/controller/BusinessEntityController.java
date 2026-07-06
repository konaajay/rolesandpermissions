package com.project.www.tenant.controller;

import com.project.www.tenant.entity.BusinessEntity;
import com.project.www.tenant.repository.BusinessEntityRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business-entities")
@RequiredArgsConstructor
public class BusinessEntityController {

    private final BusinessEntityRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY_PROFILE_VIEW') or @permissionEvaluator.hasPermission('SETTINGS_MANAGE_BUSINESS_ENTITIES')")
    public List<BusinessEntity> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        return repository.findByTenantId(tenantId);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('COMPANY_PROFILE_VIEW') or @permissionEvaluator.hasPermission('SETTINGS_MANAGE_BUSINESS_ENTITIES')")
    public List<BusinessEntity> getActive() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        return repository.findByTenantIdAndActiveTrue(tenantId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public BusinessEntity create(@RequestBody BusinessEntity entity) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        entity.setTenantId(tenantId);
        return repository.save(entity);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public BusinessEntity update(@PathVariable Long id, @RequestBody BusinessEntity details) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        BusinessEntity existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (!existing.getTenantId().equals(tenantId)) throw new RuntimeException("Unauthorized");
        
        existing.setEntityCode(details.getEntityCode());
        existing.setCompanyName(details.getCompanyName());
        existing.setDescription(details.getDescription());
        existing.setActive(details.getActive());
        existing.setShowInUserForm(details.getShowInUserForm());
        
        return repository.save(existing);
    }
}
