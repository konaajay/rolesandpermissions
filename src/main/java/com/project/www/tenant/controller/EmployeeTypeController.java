package com.project.www.tenant.controller;

import com.project.www.tenant.entity.EmployeeType;
import com.project.www.tenant.repository.EmployeeTypeRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee-types")
@RequiredArgsConstructor
public class EmployeeTypeController {

    private final EmployeeTypeRepository repository;

    @GetMapping
    @PreAuthorize("@permissionEvaluator.hasPermission('SETTINGS_MANAGE_SETTINGS')")
    public ResponseEntity<List<EmployeeType>> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(repository.findByTenantId(tenantId));
    }

    @GetMapping("/active")
    @PreAuthorize("@permissionEvaluator.hasPermission('USER_VIEW')")
    public ResponseEntity<List<EmployeeType>> getActive() {
        Long tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(repository.findByTenantIdAndActiveTrue(tenantId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission('SETTINGS_MANAGE_SETTINGS')")
    public ResponseEntity<EmployeeType> getById(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        EmployeeType existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        return ResponseEntity.ok(existing);
    }

    @PostMapping
    @PreAuthorize("@permissionEvaluator.hasPermission('SETTINGS_MANAGE_SETTINGS')")
    public ResponseEntity<EmployeeType> create(@RequestBody EmployeeType entity) {
        entity.setTenantId(TenantContext.getCurrentTenant());
        if (entity.getActive() == null) entity.setActive(true);
        if (entity.getShowInUserForm() == null) entity.setShowInUserForm(true);
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission('SETTINGS_MANAGE_SETTINGS')")
    public ResponseEntity<EmployeeType> update(@PathVariable Long id, @RequestBody EmployeeType req) {
        Long tenantId = TenantContext.getCurrentTenant();
        EmployeeType existing = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        existing.setName(req.getName());
        existing.setDescription(req.getDescription());
        if (req.getActive() != null) existing.setActive(req.getActive());
        if (req.getShowInUserForm() != null) existing.setShowInUserForm(req.getShowInUserForm());
        return ResponseEntity.ok(repository.save(existing));
    }
}
