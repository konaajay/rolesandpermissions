package com.project.www.tenant.controller;

import com.project.www.tenant.entity.Department;
import com.project.www.tenant.repository.DepartmentRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_VIEW)")
    public List<Department> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        return repository.findByTenantId(tenantId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public Department create(@RequestBody Department dept) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        dept.setTenantId(tenantId);
        return repository.save(dept);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public Department update(@PathVariable Long id, @RequestBody Department details) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        Department existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (!existing.getTenantId().equals(tenantId)) throw new RuntimeException("Unauthorized");
        
        existing.setDeptCode(details.getDeptCode());
        existing.setDeptName(details.getDeptName());
        existing.setDescription(details.getDescription());
        existing.setEntityId(details.getEntityId());
        existing.setActive(details.getActive());
        existing.setShowInUserForm(details.getShowInUserForm());
        
        return repository.save(existing);
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public Department toggle(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        Department existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (!existing.getTenantId().equals(tenantId)) throw new RuntimeException("Unauthorized");
        
        existing.setActive(!existing.getActive());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).COMPANY_PROFILE_UPDATE)")
    public void delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RuntimeException("No tenant context");
        Department existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (!existing.getTenantId().equals(tenantId)) throw new RuntimeException("Unauthorized");
        
        repository.delete(existing);
    }
}
