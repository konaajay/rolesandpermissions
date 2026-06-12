package com.project.www.tenant.controller;

import com.project.www.tenant.entity.Designation;
import com.project.www.tenant.repository.DesignationRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationRepository repository;

    @GetMapping
    @PreAuthorize("@moduleEvaluator.hasPermission('TENANT_SETTINGS_VIEW')")
    public ResponseEntity<List<Designation>> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(repository.findByTenantId(tenantId));
    }

    @GetMapping("/active")
    @PreAuthorize("@moduleEvaluator.hasPermission('USER_VIEW')")
    public ResponseEntity<List<Designation>> getActive() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(repository.findByTenantIdAndActiveTrue(tenantId));
    }

    @PostMapping
    @PreAuthorize("@moduleEvaluator.hasPermission('TENANT_SETTINGS_UPDATE')")
    public ResponseEntity<Designation> create(@RequestBody Designation entity) {
        entity.setTenantId(TenantContext.getCurrentTenant());
        if (entity.getActive() == null) entity.setActive(true);
        if (entity.getShowInUserForm() == null) entity.setShowInUserForm(true);
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduleEvaluator.hasPermission('TENANT_SETTINGS_UPDATE')")
    public ResponseEntity<Designation> update(@PathVariable Long id, @RequestBody Designation req) {
        Long tenantId = TenantContext.getCurrentTenant();
        Designation existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        if (req.getActive() != null) existing.setActive(req.getActive());
        if (req.getShowInUserForm() != null) existing.setShowInUserForm(req.getShowInUserForm());
        return ResponseEntity.ok(repository.save(existing));
    }
}
