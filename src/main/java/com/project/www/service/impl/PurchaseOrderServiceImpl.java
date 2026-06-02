package com.project.www.service.impl;

import com.project.www.dto.PurchaseOrderDto;
import com.project.www.entity.PurchaseOrder;
import com.project.www.entity.Vendor;
import com.project.www.mapper.PurchaseOrderMapper;
import com.project.www.repository.PurchaseOrderRepository;
import com.project.www.repository.VendorRepository;
import com.project.www.service.PurchaseOrderService;
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
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository repository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderMapper mapper;

    @Override
    @Transactional
    public PurchaseOrderDto createPO(PurchaseOrderDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        
        Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        PurchaseOrder entity = mapper.toEntity(dto);
        entity.setVendor(vendor);
        entity.setTenantId(tenantId);
        entity.setDeleted(false);
        
        BigDecimal total = BigDecimal.ZERO;
        if (entity.getItems() != null) {
            for (com.project.www.entity.PurchaseOrderItem item : entity.getItems()) {
                item.setPurchaseOrder(entity);
                if (item.getUnitPrice() == null) {
                    item.setUnitPrice(BigDecimal.ZERO);
                }
                item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                total = total.add(item.getTotalPrice());
            }
        }
        entity.setTotalAmount(total);
        
        PurchaseOrder saved = repository.save(entity);
        return enrichDto(mapper.toDto(saved), saved.getTotalAmount());
    }

    @Override
    @Transactional
    public PurchaseOrderDto updatePO(Long id, PurchaseOrderDto dto) {
        Long tenantId = TenantContext.getCurrentTenant();
        PurchaseOrder entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("PO not found"));
        
        entity.getItems().clear();
        mapper.updateEntityFromDto(dto, entity);
        
        if (dto.getVendorId() != null && !dto.getVendorId().equals(entity.getVendor().getId())) {
            Vendor vendor = vendorRepository.findByIdAndTenantIdAndDeletedFalse(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
            entity.setVendor(vendor);
        }
        
        BigDecimal total = BigDecimal.ZERO;
        if (entity.getItems() != null) {
            for (com.project.www.entity.PurchaseOrderItem item : entity.getItems()) {
                item.setPurchaseOrder(entity);
                if (item.getUnitPrice() == null) {
                    item.setUnitPrice(BigDecimal.ZERO);
                }
                item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                total = total.add(item.getTotalPrice());
            }
        }
        entity.setTotalAmount(total);
        
        PurchaseOrder saved = repository.save(entity);
        return enrichDto(mapper.toDto(saved), saved.getTotalAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto getPOById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .map(entity -> enrichDto(mapper.toDto(entity), entity.getTotalAmount()))
                .orElseThrow(() -> new RuntimeException("PO not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> getAllPOs() {
        Long tenantId = TenantContext.getCurrentTenant();
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId).stream()
                .map(entity -> enrichDto(mapper.toDto(entity), entity.getTotalAmount()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePO(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        PurchaseOrder entity = repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new RuntimeException("PO not found"));
        entity.setDeleted(true);
        repository.save(entity);
    }
    
    private PurchaseOrderDto enrichDto(PurchaseOrderDto dto, BigDecimal totalAmount) {
        dto.setAmountFormatted("$" + NumberFormat.getNumberInstance(Locale.US).format(totalAmount));
        return dto;
    }
}
