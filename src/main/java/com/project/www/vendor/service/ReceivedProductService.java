package com.project.www.vendor.service;

import com.project.www.vendor.dto.ProductAssignmentDto;
import com.project.www.vendor.dto.ProductAssignmentRequest;
import com.project.www.vendor.dto.ReceivedProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ReceivedProductService {
    
    ReceivedProductDto receiveRequirementItem(Long requirementItemId, Integer quantity);
    
    Page<ReceivedProductDto> getReceivedProducts(Pageable pageable);
    
    List<ReceivedProductDto> getReceivedProductsByRequirementId(Long requirementId);
    
    ProductAssignmentDto assignProduct(Long receivedProductId, ProductAssignmentRequest request);
    
    Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable);
    Page<ProductAssignmentDto> getAllAssignments(Pageable pageable);
    ReceivedProductDto returnProduct(Long receivedProductId, Integer quantity);

    ProductAssignmentDto reportDamage(Long assignmentId, String description);
    ProductAssignmentDto sendForRepair(Long assignmentId, String description);
    ProductAssignmentDto completeRepair(Long assignmentId, String description);
    ProductAssignmentDto markConsumed(Long assignmentId, String description);
    ProductAssignmentDto markNotRepairable(Long assignmentId, String description);
    ProductAssignmentDto returnAssignment(Long assignmentId, String description);
    java.util.List<com.project.www.vendor.dto.ProductLifecycleEventDto> getAssignmentHistory(Long assignmentId);

}
