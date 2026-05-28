package com.project.www.service;

import com.project.www.dto.CreateTenantRequest;
import com.project.www.dto.TenantResponse;

import java.util.List;

public interface TenantService {
    TenantResponse createTenant(CreateTenantRequest request);
    List<TenantResponse> getAllTenants();
    void enableTenant(Long id);
    void disableTenant(Long id);
}
