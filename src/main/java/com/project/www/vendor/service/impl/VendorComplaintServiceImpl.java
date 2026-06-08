package com.project.www.vendor.service.impl;

import com.project.www.vendor.repository.VendorRepository;


import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorComplaintDto;
import com.project.www.vendor.entity.Vendor;
import com.project.www.vendor.entity.VendorComplaint;
import com.project.www.vendor.repository.VendorComplaintRepository;
import com.project.www.vendor.repository.VendorRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorComplaintServiceImpl {

    private final VendorComplaintRepository repository;
    private final VendorRepository vendorRepository;

    @Transactional
    public VendorComplaintDto create(VendorComplaintDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        VendorComplaint entity = VendorComplaint.builder()
                .tenantId(tenantId)
                .vendor(vendor)
                .productOrService(dto.getProductOrService())
                .complaintType(dto.getComplaintType())
                .severity(dto.getSeverity())
                .description(dto.getDescription())
                .status("Open")
                .dateReported(dto.getDateReported() != null ? dto.getDateReported() : LocalDate.now().toString())
                .deleted(false)
                .build();

        return toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<VendorComplaintDto> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public VendorComplaintDto update(Long id, VendorComplaintDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorComplaint entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (dto.getStatus()           != null) entity.setStatus(dto.getStatus());
        if (dto.getResolution()       != null) entity.setResolution(dto.getResolution());
        if (dto.getResolvedDate()     != null) entity.setResolvedDate(dto.getResolvedDate());
        if (dto.getSeverity()         != null) entity.setSeverity(dto.getSeverity());
        if (dto.getDescription()      != null) entity.setDescription(dto.getDescription());
        if (dto.getProductOrService() != null) entity.setProductOrService(dto.getProductOrService());
        if (dto.getComplaintType()    != null) entity.setComplaintType(dto.getComplaintType());

        // Auto-set resolvedDate when status changes to Resolved/Closed
        if (("Resolved".equals(dto.getStatus()) || "Closed".equals(dto.getStatus()))
                && entity.getResolvedDate() == null) {
            entity.setResolvedDate(LocalDate.now().toString());
        }

        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorComplaint entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        entity.setDeleted(true);
        repository.save(entity);
    }

    private VendorComplaintDto toDto(VendorComplaint e) {
        return VendorComplaintDto.builder()
                .id(e.getId())
                .vendorId(e.getVendor().getId())
                .vendorName(e.getVendor().getVendorName())
                .productOrService(e.getProductOrService())
                .complaintType(e.getComplaintType())
                .severity(e.getSeverity())
                .description(e.getDescription())
                .status(e.getStatus())
                .dateReported(e.getDateReported())
                .resolvedDate(e.getResolvedDate())
                .resolution(e.getResolution())
                .build();
    }
}
