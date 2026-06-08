package com.project.www.vendor.service;

import com.project.www.vendor.dto.PurchaseOrderDto;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderDto createPO(PurchaseOrderDto dto);
    PurchaseOrderDto updatePO(Long id, PurchaseOrderDto dto);
    PurchaseOrderDto getPOById(Long id);
    List<PurchaseOrderDto> getAllPOs();
    void deletePO(Long id);
}
