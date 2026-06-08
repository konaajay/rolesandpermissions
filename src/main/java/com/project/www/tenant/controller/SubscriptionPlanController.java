package com.project.www.tenant.controller;

import com.project.www.tenant.entity.SubscriptionPlan;
import com.project.www.tenant.repository.SubscriptionPlanRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanRepository repository;

    @GetMapping
    public List<SubscriptionPlan> getActivePlans() {
        return repository.findByActiveTrue();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_VIEW)")
    public List<SubscriptionPlan> getAllPlans() {
        Long tenantId = TenantContext.getCurrentTenant();
        // Allow System Admin
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_CREATE)")
    public SubscriptionPlan createPlan(@RequestBody SubscriptionPlan plan) {
        return repository.save(plan);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_CREATE)")
    public SubscriptionPlan updatePlan(@PathVariable Long id, @RequestBody SubscriptionPlan details) {
        SubscriptionPlan existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Plan not found"));
        existing.setName(details.getName());
        existing.setDescription(details.getDescription());
        existing.setMonthlyPrice(details.getMonthlyPrice());
        existing.setYearlyPrice(details.getYearlyPrice());
        existing.setMaxUsers(details.getMaxUsers());
        existing.setModules(details.getModules());
        existing.setActive(details.getActive());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).TENANT_CREATE)")
    public void deletePlan(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
