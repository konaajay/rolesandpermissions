package com.project.www.tenant.controller;

import com.project.www.tenant.entity.Tenant;

import com.project.www.tenant.dto.TenantModuleUpdateRequest;

import com.project.www.tenant.dto.CreateTenantRequest;
import com.project.www.tenant.dto.TenantResponse;
import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.service.TenantService;
import com.project.www.tenant.service.TenantModuleService;
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
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private TenantService tenantService;
    
    private final TenantModuleService tenantModuleService;

    @PostMapping
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_CREATE')")
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_VIEW')")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_VIEW') or #id == T(com.project.www.util.TenantContext).getCurrentTenant()")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_ENABLE')")
    public ResponseEntity<String> enableTenant(@PathVariable Long id) {
        tenantService.enableTenant(id);
        return ResponseEntity.ok("Tenant Enabled Successfully");
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_DISABLE')")
    public ResponseEntity<String> disableTenant(@PathVariable Long id) {
        tenantService.disableTenant(id);
        return ResponseEntity.ok("Tenant Disabled Successfully");
    }

    @GetMapping("/{id}/modules")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_VIEW')")
    public ResponseEntity<List<TenantModule>> getTenantModules(@PathVariable Long id) {
        return ResponseEntity.ok(tenantModuleService.getModulesForTenant(id));
    }

    @GetMapping("/current/modules")
    public ResponseEntity<List<TenantModule>> getCurrentTenantModules() {
        Long tenantId = com.project.www.util.TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tenantModuleService.getModulesForTenant(tenantId));
    }

    @PutMapping("/{id}/modules/{moduleName}/enable")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_ENABLE')")
    public ResponseEntity<String> enableTenantModule(
            @PathVariable Long id, 
            @PathVariable String moduleName,
            @RequestBody(required = false) com.project.www.tenant.dto.TenantModuleUpdateRequest request) {
        tenantModuleService.enableModule(id, moduleName, request);
        return ResponseEntity.ok("Module Enabled/Updated Successfully");
    }

    @PutMapping("/{id}/modules/{moduleName}/disable")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_DISABLE')")
    public ResponseEntity<String> disableTenantModule(@PathVariable Long id, @PathVariable String moduleName) {
        tenantModuleService.disableModule(id, moduleName);
        return ResponseEntity.ok("Module Disabled Successfully");
    }

    @PutMapping("/{id}/modules/bulk")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_UPDATE') or @permissionEvaluator.hasPermission('SUBSCRIPTION_MANAGE') or #id == T(com.project.www.util.TenantContext).getCurrentTenant()")
    public ResponseEntity<String> saveBulkModules(@PathVariable Long id, @RequestBody com.project.www.tenant.dto.BulkModuleSaveRequest request) {
        String originalCode = com.project.www.util.TenantContext.getCurrentTenantCode();
        Long originalTenantId = com.project.www.util.TenantContext.getCurrentTenant();
        try {
            com.project.www.util.TenantContext.clear();
            System.out.println("RECEIVED BULK MODULE SAVE REQUEST: " + request);
            tenantModuleService.saveBulkModules(id, request);
            return ResponseEntity.ok("Bulk Modules Saved and Invoice Generated");
        } finally {
            com.project.www.util.TenantContext.setCurrentTenant(originalTenantId);
            com.project.www.util.TenantContext.setCurrentTenantCode(originalCode);
        }
    }

    @GetMapping("/{id}/invoices")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_VIEW') or @permissionEvaluator.hasPermission('SUBSCRIPTION_MANAGE') or #id == T(com.project.www.util.TenantContext).getCurrentTenant()")
    public ResponseEntity<List<com.project.www.tenant.entity.TenantInvoice>> getTenantInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(tenantModuleService.getInvoicesForTenant(id));
    }
    @GetMapping("/{id}/invoices/{invoiceId}/installments")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_VIEW') or @permissionEvaluator.hasPermission('SUBSCRIPTION_MANAGE') or #id == T(com.project.www.util.TenantContext).getCurrentTenant()")
    public ResponseEntity<List<com.project.www.tenant.entity.TenantInvoiceInstallment>> getInvoiceInstallments(
            @PathVariable Long id, @PathVariable Long invoiceId) {
        return ResponseEntity.ok(tenantModuleService.getInstallmentsForInvoice(invoiceId));
    }

    @GetMapping("/{id}/invoices/{invoiceId}/items")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_VIEW') or @permissionEvaluator.hasPermission('SUBSCRIPTION_MANAGE') or #id == T(com.project.www.util.TenantContext).getCurrentTenant()")
    public ResponseEntity<List<com.project.www.tenant.entity.TenantInvoiceItem>> getInvoiceItems(
            @PathVariable Long id, @PathVariable Long invoiceId) {
        return ResponseEntity.ok(tenantModuleService.getItemsForInvoice(invoiceId));
    }

    @PutMapping("/{id}/invoices/{invoiceId}/installments/{installmentId}/pay")
    @PreAuthorize("@permissionEvaluator.hasPermission('TENANT_UPDATE') or @permissionEvaluator.hasPermission('SUBSCRIPTION_MANAGE') or #id == T(com.project.www.util.TenantContext).getCurrentTenant()")
    public ResponseEntity<String> payInstallment(
            @PathVariable Long id, @PathVariable Long invoiceId, @PathVariable Long installmentId) {
        String originalCode = com.project.www.util.TenantContext.getCurrentTenantCode();
        Long originalTenantId = com.project.www.util.TenantContext.getCurrentTenant();
        try {
            com.project.www.util.TenantContext.clear();
            tenantModuleService.payInstallment(invoiceId, installmentId);
            return ResponseEntity.ok("Installment paid successfully");
        } finally {
            com.project.www.util.TenantContext.setCurrentTenant(originalTenantId);
            com.project.www.util.TenantContext.setCurrentTenantCode(originalCode);
        }
    }
}
