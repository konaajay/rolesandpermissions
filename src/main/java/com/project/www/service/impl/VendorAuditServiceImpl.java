package com.project.www.service.impl;

import com.project.www.dto.VendorAuditDto;
import com.project.www.entity.Vendor;
import com.project.www.entity.VendorAudit;
import com.project.www.mapper.VendorAuditMapper;
import com.project.www.repository.VendorAuditRepository;
import com.project.www.repository.VendorRepository;
import com.project.www.service.VendorAuditService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorAuditServiceImpl implements VendorAuditService {

    private final VendorAuditRepository repository;
    private final VendorRepository vendorRepository;
    private final VendorAuditMapper mapper;

    @Override
    @Transactional
    public VendorAuditDto createAudit(VendorAuditDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        
        Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        VendorAudit entity = mapper.toEntity(dto);
        entity.setVendor(vendor);
        entity.setTenantId(tenantId);
        entity.setDeleted(false);
        
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public VendorAuditDto updateAudit(Long id, VendorAuditDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorAudit entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        
        mapper.updateEntityFromDto(dto, entity);
        
        if (dto.getVendorId() != null && !dto.getVendorId().equals(entity.getVendor().getId())) {
            Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
            entity.setVendor(vendor);
        }
        
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorAuditDto getAuditById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorAuditDto> getAllAudits() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAudit(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorAudit entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        entity.setDeleted(true);
        repository.save(entity);
    }
}
