package com.project.www.vendor.service.impl;

import com.project.www.vendor.dto.VendorCategoryDto;
import com.project.www.vendor.entity.VendorCategory;
import com.project.www.vendor.mapper.VendorCategoryMapper;
import com.project.www.vendor.repository.VendorCategoryRepository;
import com.project.www.vendor.service.VendorCategoryService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorCategoryServiceImpl implements VendorCategoryService {

    private final VendorCategoryRepository repository;
    private final VendorCategoryMapper mapper;

    @Override
    @Transactional
    public VendorCategoryDto createCategory(VendorCategoryDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (repository.existsByTenantIdAndNameAndDeletedFalse(tenantId, dto.getName())) {
            throw new RuntimeException("Category name already exists");
        }
        VendorCategory entity = mapper.toEntity(dto);
        entity.setTenantId(tenantId);
        entity.setActive(true);
        entity.setDeleted(false);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public VendorCategoryDto updateCategory(Long id, VendorCategoryDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorCategory entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (!entity.getName().equals(dto.getName()) && repository.existsByTenantIdAndNameAndDeletedFalse(tenantId, dto.getName())) {
            throw new RuntimeException("Category name already exists");
        }
        
        mapper.updateEntityFromDto(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorCategoryDto getCategoryById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorCategoryDto> getAllCategories() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalse(tenantId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorCategory entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        entity.setDeleted(true);
        entity.setActive(false);
        repository.save(entity);
    }
}
