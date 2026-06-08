package com.project.www.vendor.service.impl;

import com.project.www.tenant.entity.Tenant;

import com.project.www.vendor.dto.VendorDto;
import com.project.www.vendor.entity.Vendor;
import com.project.www.vendor.exception.DuplicateVendorException;
import com.project.www.vendor.exception.VendorNotFoundException;
import com.project.www.vendor.mapper.VendorMapper;
import com.project.www.vendor.repository.VendorCategoryRepository;
import com.project.www.vendor.entity.VendorCategory;
import com.project.www.vendor.repository.VendorRepository;
import com.project.www.vendor.service.VendorService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final VendorCategoryRepository vendorCategoryRepository;
    private final VendorMapper vendorMapper;

    // Placeholder for document storage path, should be configured in application.properties
    private final String UPLOAD_DIR = "uploads/vendors/";

    @Override
    @Transactional
    public VendorDto createVendor(VendorDto vendorDto) {
        Long tenantId = TenantContext.getCurrentTenant();
        validateVendorForCreation(vendorDto, tenantId);

        Vendor vendor = vendorMapper.toEntity(vendorDto);
        
        if (vendorDto.getCategoryId() != null) {
            VendorCategory category = vendorCategoryRepository.findByIdAndTenantIdAndDeletedFalse(vendorDto.getCategoryId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            vendor.setCategory(category);
        }

        vendor.setTenantId(tenantId);
        vendor.setActive(true);
        vendor.setDeleted(false);
        vendor.setStatus("ACTIVE");

        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendor created with ID: {} for Tenant ID: {}", savedVendor.getId(), tenantId);
        return vendorMapper.toDto(savedVendor);
    }

    @Override
    @Transactional
    public VendorDto updateVendor(Long id, VendorDto vendorDto) {
        Long tenantId = TenantContext.getCurrentTenant();
        Vendor existingVendor = getVendorEntityByIdAndTenant(id, tenantId);

        validateVendorForUpdate(vendorDto, existingVendor, tenantId);

        vendorMapper.updateEntityFromDto(vendorDto, existingVendor);
        
        if (vendorDto.getCategoryId() != null) {
            VendorCategory category = vendorCategoryRepository.findByIdAndTenantIdAndDeletedFalse(vendorDto.getCategoryId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            existingVendor.setCategory(category);
        }

        Vendor updatedVendor = vendorRepository.save(existingVendor);
        log.info("Vendor updated with ID: {} for Tenant ID: {}", id, tenantId);
        return vendorMapper.toDto(updatedVendor);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDto getVendorById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        Vendor vendor = getVendorEntityByIdAndTenant(id, tenantId);
        return vendorMapper.toDto(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorDto> getAllVendors(int page, int size, String sortBy, String sortDir) {
        Long tenantId = TenantContext.getCurrentTenant();
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return vendorRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(vendorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorDto> searchVendors(String searchTerm, int page, int size, String sortBy, String sortDir) {
        Long tenantId = TenantContext.getCurrentTenant();
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return vendorRepository.searchVendors(tenantId, searchTerm, pageable)
                .map(vendorMapper::toDto);
    }

    @Override
    @Transactional
    public void softDeleteVendor(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        Vendor vendor = getVendorEntityByIdAndTenant(id, tenantId);
        vendor.setDeleted(true);
        vendor.setActive(false);
        vendor.setStatus("DELETED");
        vendorRepository.save(vendor);
        log.info("Vendor soft-deleted with ID: {} for Tenant ID: {}", id, tenantId);
    }

    @Override
    @Transactional
    public VendorDto toggleVendorStatus(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        Vendor vendor = getVendorEntityByIdAndTenant(id, tenantId);
        
        vendor.setActive(!vendor.getActive());
        vendor.setStatus(vendor.getActive() ? "ACTIVE" : "INACTIVE");
        
        Vendor updatedVendor = vendorRepository.save(vendor);
        log.info("Vendor status toggled to {} for ID: {} and Tenant ID: {}", vendor.getStatus(), id, tenantId);
        return vendorMapper.toDto(updatedVendor);
    }

    @Override
    @Transactional
    public VendorDto uploadVendorDocument(Long id, MultipartFile file) {
        Long tenantId = TenantContext.getCurrentTenant();
        Vendor vendor = getVendorEntityByIdAndTenant(id, tenantId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR + tenantId);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            vendor.setDocumentUrl(filePath.toString());
            Vendor updatedVendor = vendorRepository.save(vendor);
            log.info("Document uploaded for Vendor ID: {} for Tenant ID: {}", id, tenantId);
            
            return vendorMapper.toDto(updatedVendor);
        } catch (IOException e) {
            log.error("Failed to upload document for Vendor ID: {}", id, e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private Vendor getVendorEntityByIdAndTenant(Long id, Long tenantId) {
        return vendorRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new VendorNotFoundException("Vendor not found with id: " + id));
    }

    private void validateVendorForCreation(VendorDto dto, Long tenantId) {
        if (vendorRepository.existsByTenantIdAndVendorCodeAndDeletedFalse(tenantId, dto.getVendorCode())) {
            throw new DuplicateVendorException("Vendor Code already exists");
        }
        if (vendorRepository.existsByTenantIdAndEmailAndDeletedFalse(tenantId, dto.getEmail())) {
            throw new DuplicateVendorException("Email already exists");
        }
        if (vendorRepository.existsByTenantIdAndMobileNumberAndDeletedFalse(tenantId, dto.getMobileNumber())) {
            throw new DuplicateVendorException("Mobile Number already exists");
        }
    }

    private void validateVendorForUpdate(VendorDto dto, Vendor existingVendor, Long tenantId) {
        if (!existingVendor.getVendorCode().equals(dto.getVendorCode()) && 
            vendorRepository.existsByTenantIdAndVendorCodeAndDeletedFalse(tenantId, dto.getVendorCode())) {
            throw new DuplicateVendorException("Vendor Code already exists");
        }
        if (!existingVendor.getEmail().equals(dto.getEmail()) && 
            vendorRepository.existsByTenantIdAndEmailAndDeletedFalse(tenantId, dto.getEmail())) {
            throw new DuplicateVendorException("Email already exists");
        }
        if (!existingVendor.getMobileNumber().equals(dto.getMobileNumber()) && 
            vendorRepository.existsByTenantIdAndMobileNumberAndDeletedFalse(tenantId, dto.getMobileNumber())) {
            throw new DuplicateVendorException("Mobile Number already exists");
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
