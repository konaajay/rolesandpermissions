package com.project.www.tenant.controller;

import com.project.www.tenant.entity.WorkMode;
import com.project.www.tenant.repository.WorkModeRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work-modes")
@RequiredArgsConstructor
public class WorkModeController {

    private final WorkModeRepository repository;

    @GetMapping
    @PreAuthorize("@moduleEvaluator.hasPermission('TENANT_SETTINGS_VIEW')")
    public ResponseEntity<List<WorkMode>> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(repository.findByTenantId(tenantId));
    }

    @GetMapping("/active")
    @PreAuthorize("@moduleEvaluator.hasPermission('USER_VIEW')")
    public ResponseEntity<List<WorkMode>> getActive() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(repository.findByTenantIdAndActiveTrue(tenantId));
    }

    @PostMapping
    @PreAuthorize("@moduleEvaluator.hasPermission('TENANT_SETTINGS_UPDATE')")
    public ResponseEntity<WorkMode> create(@RequestBody WorkMode entity) {
        entity.setTenantId(TenantContext.getCurrentTenant());
        if (entity.getActive() == null) entity.setActive(true);
        if (entity.getShowInUserForm() == null) entity.setShowInUserForm(true);
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduleEvaluator.hasPermission('TENANT_SETTINGS_UPDATE')")
    public ResponseEntity<WorkMode> update(@PathVariable Long id, @RequestBody WorkMode req) {
        Long tenantId = TenantContext.getCurrentTenant();
        WorkMode existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        if (req.getActive() != null) existing.setActive(req.getActive());
        if (req.getShowInUserForm() != null) existing.setShowInUserForm(req.getShowInUserForm());
        return ResponseEntity.ok(repository.save(existing));
    }
}
