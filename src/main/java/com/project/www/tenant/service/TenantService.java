package com.project.www.tenant.service;

import com.project.www.tenant.dto.CreateTenantRequest;
import com.project.www.tenant.dto.TenantResponse;

import java.util.List;

public interface TenantService {
    TenantResponse createTenant(CreateTenantRequest request);
    List<TenantResponse> getAllTenants();
    void enableTenant(Long id);
    void disableTenant(Long id);
    TenantResponse getTenantById(Long id);
}
