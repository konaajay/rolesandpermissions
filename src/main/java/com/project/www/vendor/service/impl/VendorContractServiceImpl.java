package com.project.www.vendor.service.impl;

import com.project.www.vendor.dto.VendorContractDto;
import com.project.www.vendor.entity.Vendor;
import com.project.www.vendor.entity.VendorContract;
import com.project.www.vendor.mapper.VendorContractMapper;
import com.project.www.vendor.repository.VendorContractRepository;
import com.project.www.vendor.repository.VendorRepository;
import com.project.www.vendor.service.VendorContractService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorContractServiceImpl implements VendorContractService {

    private final VendorContractRepository repository;
    private final VendorRepository vendorRepository;
    private final VendorContractMapper mapper;

    @Override
    @Transactional
    public VendorContractDto createContract(VendorContractDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        
        Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        VendorContract entity = mapper.toEntity(dto);
        entity.setVendor(vendor);
        entity.setTenantId(tenantId);
        entity.setDeleted(false);
        
        entity.setAmount(parseAmount(dto.getAmount()));
        
        VendorContract saved = repository.save(entity);
        return enrichDto(mapper.toDto(saved), saved.getAmount());
    }

    @Override
    @Transactional
    public VendorContractDto updateContract(Long id, VendorContractDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorContract entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        
        mapper.updateEntityFromDto(dto, entity);
        
        if (dto.getVendorId() != null && !dto.getVendorId().equals(entity.getVendor().getId())) {
            Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
            entity.setVendor(vendor);
        }
        
        entity.setAmount(parseAmount(dto.getAmount()));
        
        VendorContract saved = repository.save(entity);
        return enrichDto(mapper.toDto(saved), saved.getAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public VendorContractDto getContractById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .map(entity -> enrichDto(mapper.toDto(entity), entity.getAmount()))
                .orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorContractDto> getAllContracts() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId).stream()
                .map(entity -> enrichDto(mapper.toDto(entity), entity.getAmount()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorContract entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        entity.setDeleted(true);
        repository.save(entity);
    }
    
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) return BigDecimal.ZERO;
        try {
            // Remove everything except numbers and decimal point
            String cleanStr = amountStr.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleanStr);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    private VendorContractDto enrichDto(VendorContractDto dto, BigDecimal amount) {
        if (amount != null) {
            dto.setAmount("$" + NumberFormat.getNumberInstance(Locale.US).format(amount));
        } else {
            dto.setAmount("$0");
        }
        return dto;
    }
}
