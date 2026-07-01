package com.project.www.invoice.service.impl;

import com.project.www.invoice.dto.InvoiceConfigurationDto;
import com.project.www.invoice.entity.InvoiceConfiguration;
import com.project.www.invoice.mapper.InvoiceConfigurationMapper;
import com.project.www.invoice.repository.InvoiceConfigurationRepository;
import com.project.www.invoice.service.InvoiceConfigurationService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceConfigurationServiceImpl implements InvoiceConfigurationService {

    private final InvoiceConfigurationRepository repository;
    private final InvoiceConfigurationMapper mapper;

    @Override
    @Transactional
    public InvoiceConfigurationDto createConfiguration(InvoiceConfigurationDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        InvoiceConfiguration entity = mapper.toEntity(dto);
        entity.setTenantId(tenantId);
        entity.setDeleted(false);
        
        if (entity.getTargetModule() == null || entity.getTargetModule().isEmpty()) {
            entity.setTargetModule("ALL");
        }

        if (Boolean.TRUE.equals(dto.getActive())) {
            repository.deactivateAllForTenantAndModule(tenantId, entity.getTargetModule());
        } else {
            entity.setActive(false);
        }

        InvoiceConfiguration saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public InvoiceConfigurationDto updateConfiguration(Long id, InvoiceConfigurationDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        InvoiceConfiguration entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice Configuration not found"));
        
        mapper.updateEntityFromDto(dto, entity);
        
        if (entity.getTargetModule() == null || entity.getTargetModule().isEmpty()) {
            entity.setTargetModule("ALL");
        }
        
        if (Boolean.TRUE.equals(dto.getActive())) {
            repository.deactivateAllForTenantAndModule(tenantId, entity.getTargetModule());
            entity.setActive(true);
        }
        
        InvoiceConfiguration saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteConfiguration(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        InvoiceConfiguration entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice Configuration not found"));
        entity.setDeleted(true);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceConfigurationDto getConfigurationById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Invoice Configuration not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceConfigurationDto> getAllConfigurationsForTenant() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalse(tenantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceConfigurationDto getActiveConfigurationForTenant() {
        // Fallback for ALL if no module specified
        return getActiveConfigurationForTenantAndModule("ALL");
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceConfigurationDto getActiveConfigurationForTenantAndModule(String targetModule) {
        Long tenantId = TenantContext.getCurrentTenant();
        // First try to find the specific module
        return repository.findByTenantIdAndTargetModuleAndActiveTrueAndDeletedFalse(tenantId, targetModule)
                .map(mapper::toDto)
                .orElseGet(() -> {
                    // Fallback to "ALL" if the specific module doesn't have an active template
                    if (!"ALL".equals(targetModule)) {
                        return repository.findByTenantIdAndTargetModuleAndActiveTrueAndDeletedFalse(tenantId, "ALL")
                                .map(mapper::toDto)
                                .orElse(null);
                    }
                    return null;
                });
    }

    @Override
    @Transactional
    public void activateConfiguration(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        InvoiceConfiguration entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice Configuration not found"));
        
        repository.deactivateAllForTenantAndModule(tenantId, entity.getTargetModule() != null ? entity.getTargetModule() : "ALL");
        entity.setActive(true);
        repository.save(entity);
    }
}
