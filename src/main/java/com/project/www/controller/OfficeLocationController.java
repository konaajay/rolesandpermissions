package com.project.www.controller;

import com.project.www.entity.OfficeLocation;
import com.project.www.repository.OfficeLocationRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/office-locations")
@RequiredArgsConstructor
public class OfficeLocationController {

    private final OfficeLocationRepository officeLocationRepository;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<OfficeLocation> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        return officeLocationRepository.findAllByTenantId(tenantId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public OfficeLocation create(@RequestBody OfficeLocation location) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        location.setTenantId(tenantId);
        return officeLocationRepository.save(location);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public OfficeLocation update(@PathVariable Long id, @RequestBody OfficeLocation details) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        OfficeLocation existing = officeLocationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Office location not found"));
        
        existing.setName(details.getName());
        existing.setLatitude(details.getLatitude());
        existing.setLongitude(details.getLongitude());
        existing.setRadiusMeters(details.getRadiusMeters());
        existing.setTrackingIntervalSec(details.getTrackingIntervalSec());
        existing.setMaxAccuracyMeters(details.getMaxAccuracyMeters());
        existing.setMaxIdleMinutes(details.getMaxIdleMinutes());
        
        return officeLocationRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public void delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        OfficeLocation existing = officeLocationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Office location not found"));
        officeLocationRepository.delete(existing);
    }
}
