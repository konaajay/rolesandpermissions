package com.project.www.service.impl;

import com.project.www.dto.VendorInvoiceDto;
import com.project.www.entity.Vendor;
import com.project.www.entity.VendorInvoice;
import com.project.www.mapper.VendorInvoiceMapper;
import com.project.www.repository.VendorInvoiceRepository;
import com.project.www.repository.VendorRepository;
import com.project.www.service.VendorInvoiceService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorInvoiceServiceImpl implements VendorInvoiceService {

    private final VendorInvoiceRepository repository;
    private final VendorRepository vendorRepository;
    private final VendorInvoiceMapper mapper;
    private final com.project.www.service.PdfGenerationService pdfGenerationService;

    @Override
    @Transactional
    public VendorInvoiceDto createInvoice(VendorInvoiceDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        
        Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        VendorInvoice entity = mapper.toEntity(dto);
        entity.setVendor(vendor);
        entity.setTenantId(tenantId);
        entity.setDeleted(false);
        entity.setInvoiceNumber(UUID.randomUUID().toString()); // Placeholder, mapper will use ID to inv
        
        entity.setAmount(parseAmount(dto.getAmount()));
        
        VendorInvoice saved = repository.save(entity);
        return enrichDto(mapper.toDto(saved), saved.getAmount(), saved.getReceiptUrl());
    }

    @Override
    @Transactional
    public VendorInvoiceDto updateInvoice(Long id, VendorInvoiceDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorInvoice entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        mapper.updateEntityFromDto(dto, entity);
        
        if (dto.getVendorId() != null && !dto.getVendorId().equals(entity.getVendor().getId())) {
            Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
            entity.setVendor(vendor);
        }
        
        entity.setAmount(parseAmount(dto.getAmount()));
        
        if ("Paid".equalsIgnoreCase(dto.getStatus()) && entity.getReceiptUrl() == null) {
            String receiptUrl = pdfGenerationService.generatePaymentReceipt(entity);
            entity.setReceiptUrl(receiptUrl);
        }
        
        VendorInvoice saved = repository.save(entity);
        return enrichDto(mapper.toDto(saved), saved.getAmount(), saved.getReceiptUrl());
    }

    @Override
    @Transactional(readOnly = true)
    public VendorInvoiceDto getInvoiceById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .map(entity -> enrichDto(mapper.toDto(entity), entity.getAmount(), entity.getReceiptUrl()))
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorInvoiceDto> getAllInvoices() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId).stream()
                .map(entity -> enrichDto(mapper.toDto(entity), entity.getAmount(), entity.getReceiptUrl()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        VendorInvoice entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        entity.setDeleted(true);
        repository.save(entity);
    }
    
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) return BigDecimal.ZERO;
        try {
            String cleanStr = amountStr.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleanStr);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    private VendorInvoiceDto enrichDto(VendorInvoiceDto dto, BigDecimal amount, String receiptUrl) {
        if (amount != null) {
            dto.setAmount("$" + NumberFormat.getNumberInstance(Locale.US).format(amount));
            dto.setAmountValue(amount);
        } else {
            dto.setAmount("$0");
        }
        dto.setReceiptUrl(receiptUrl);
        return dto;
    }
}
