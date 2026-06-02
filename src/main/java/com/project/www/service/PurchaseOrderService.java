package com.project.www.service;

import com.project.www.dto.PurchaseOrderDto;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderDto createPO(PurchaseOrderDto dto);
    PurchaseOrderDto updatePO(Long id, PurchaseOrderDto dto);
    PurchaseOrderDto getPOById(Long id);
    List<PurchaseOrderDto> getAllPOs();
    void deletePO(Long id);
}
