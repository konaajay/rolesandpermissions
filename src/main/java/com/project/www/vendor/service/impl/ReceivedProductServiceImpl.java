package com.project.www.vendor.service.impl;

import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.security.UserContext;
import com.project.www.util.TenantContext;
import com.project.www.vendor.dto.ProductAssignmentDto;
import com.project.www.vendor.dto.ProductAssignmentRequest;
import com.project.www.vendor.dto.ReceivedProductDto;
import com.project.www.vendor.entity.ProductAssignment;
import com.project.www.vendor.entity.ReceivedProduct;
import com.project.www.vendor.entity.RequirementItem;
import com.project.www.vendor.entity.Requirement;
import com.project.www.vendor.exception.VendorNotFoundException;
import com.project.www.vendor.repository.ProductAssignmentRepository;
import com.project.www.vendor.repository.ReceivedProductRepository;
import com.project.www.vendor.repository.RequirementItemRepository;
import com.project.www.vendor.service.ReceivedProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceivedProductServiceImpl implements ReceivedProductService {

    private final ReceivedProductRepository receivedProductRepository;
    private final ProductAssignmentRepository productAssignmentRepository;
    private final com.project.www.vendor.repository.ProductLifecycleEventRepository productLifecycleEventRepository;
    private final RequirementItemRepository requirementItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReceivedProductDto receiveRequirementItem(Long requirementItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Received quantity must be positive");
        }
        Long tenantId = TenantContext.getCurrentTenant();
        RequirementItem item = requirementItemRepository.findById(requirementItemId)
                .orElseThrow(() -> new RuntimeException("Requirement Item not found"));

        Requirement requirement = item.getRequirement();
        if (!requirement.getVendor().getTenantId().equals(tenantId)) {
            throw new RuntimeException("Item does not belong to current tenant");
        }

        ReceivedProduct rp = receivedProductRepository
                .findByRequirementItemIdAndTenantIdAndDeletedFalse(requirementItemId, tenantId)
                .orElseGet(() -> ReceivedProduct.builder()
                        .tenantId(tenantId)
                        .vendor(requirement.getVendor())
                        .requirementItem(item)
                        .receivedQuantity(0)
                        .assignedQuantity(0)
                        .deleted(false)
                        .build());

        rp.setReceivedQuantity(rp.getReceivedQuantity() + quantity);
        updateStatus(rp);

        receivedProductRepository.save(rp);
        return mapToDto(rp);
    }

    @Transactional(readOnly = true)
    public Page<ReceivedProductDto> getReceivedProducts(Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenant();
        return receivedProductRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReceivedProductDto> getReceivedProductsByRequirementId(Long requirementId) {
        Long tenantId = TenantContext.getCurrentTenant();
        return receivedProductRepository
                .findByRequirementItem_Requirement_IdAndTenantIdAndDeletedFalse(requirementId, tenantId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductAssignmentDto assignProduct(Long receivedProductId, ProductAssignmentRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) userId = 1L;

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        ReceivedProduct rp = receivedProductRepository.findByIdAndTenantIdAndDeletedFalse(receivedProductId, tenantId)
                .orElseThrow(() -> new RuntimeException("Received product not found"));

        int available = rp.getReceivedQuantity() - rp.getAssignedQuantity();
        if (request.getQuantity() > available) {
            throw new IllegalArgumentException("Requested quantity exceeds available quantity");
        }

        User assignedUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (assignedUser.getTenantId() != null && !assignedUser.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User does not belong to current tenant");
        }

        if (assignedUser.getActive() != null && !assignedUser.getActive()) {
            throw new RuntimeException("Assigned user is not active");
        }

        rp.setAssignedQuantity(rp.getAssignedQuantity() + request.getQuantity());
        updateStatus(rp);
        receivedProductRepository.save(rp);

        String itemType = rp.getRequirementItem().getItemType();
        if (itemType == null)
            itemType = "ASSET";

        ProductAssignment firstAssignment = null;

        if ("ASSET".equals(itemType) && request.getQuantity() > 1) {
            // Split into individual assignments
            for (int i = 0; i < request.getQuantity(); i++) {
                ProductAssignment assignment = ProductAssignment.builder()
                        .tenantId(tenantId)
                        .receivedProduct(rp)
                        .assignedUser(assignedUser)
                        .quantity(1)
                        .assignedAt(java.time.LocalDateTime.now())
                        .assignedBy(userId)
                        .deleted(false)
                        .status("ASSIGNED")
                        .build();
                productAssignmentRepository.save(assignment);
                createLifecycleEvent(assignment, "ASSIGNED", null, "ASSIGNED", "Initial Assignment");
                if (firstAssignment == null)
                    firstAssignment = assignment;
            }
        } else {
            ProductAssignment assignment = ProductAssignment.builder()
                    .tenantId(tenantId)
                    .receivedProduct(rp)
                    .assignedUser(assignedUser)
                    .quantity(request.getQuantity())
                    .assignedAt(java.time.LocalDateTime.now())
                    .assignedBy(userId)
                    .deleted(false)
                    .status("ASSIGNED")
                    .build();
            productAssignmentRepository.save(assignment);
            createLifecycleEvent(assignment, "ASSIGNED", null, "ASSIGNED", "Initial Assignment");
            firstAssignment = assignment;
        }

        return ProductAssignmentDto.builder()
                .id(firstAssignment.getId())
                .receivedProductId(rp.getId())
                .productName(rp.getRequirementItem().getItemName())
                .userId(assignedUser.getId())
                .userName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                .quantity(firstAssignment.getQuantity())
                .assignedAt(firstAssignment.getAssignedAt())
                .assignedBy(firstAssignment.getAssignedBy())
                .status(firstAssignment.getStatus())
                .itemType(itemType)
                .build();
    }

    private void createLifecycleEvent(ProductAssignment assignment, String eventType, String oldStatus,
            String newStatus, String description) {
        Long performedBy = com.project.www.security.UserContext.getCurrentUserId();
        if (performedBy == null) performedBy = 1L;
        com.project.www.vendor.entity.ProductLifecycleEvent event = com.project.www.vendor.entity.ProductLifecycleEvent
                .builder()
                .tenantId(assignment.getTenantId())
                .assignment(assignment)
                .eventType(eventType)
                .previousStatus(oldStatus)
                .newStatus(newStatus)
                .performedBy(performedBy)
                .assignedTo(assignment.getAssignedUser().getId())
                .description(description)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        productLifecycleEventRepository.save(event);
    }

    private ProductAssignment getAssignmentForTransition(Long assignmentId) {
        Long tenantId = com.project.www.util.TenantContext.getCurrentTenant();
        return productAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getTenantId().equals(tenantId) && !a.getDeleted())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Override
    @Transactional
    public ProductAssignmentDto reportDamage(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        if (!"ASSIGNED".equals(assignment.getStatus()))
            throw new IllegalStateException("Only ASSIGNED assets can be reported damaged");
        String oldStatus = assignment.getStatus();
        assignment.setStatus("DAMAGED");
        productAssignmentRepository.save(assignment);
        createLifecycleEvent(assignment, "DAMAGE_REPORTED", oldStatus, "DAMAGED", description);
        return mapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public ProductAssignmentDto sendForRepair(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        if (!"DAMAGED".equals(assignment.getStatus()))
            throw new IllegalStateException("Only DAMAGED assets can be sent for repair");
        String oldStatus = assignment.getStatus();
        assignment.setStatus("UNDER_REPAIR");
        productAssignmentRepository.save(assignment);
        createLifecycleEvent(assignment, "SENT_FOR_REPAIR", oldStatus, "UNDER_REPAIR", description);
        return mapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public ProductAssignmentDto completeRepair(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        if (!"UNDER_REPAIR".equals(assignment.getStatus()))
            throw new IllegalStateException("Asset is not under repair");
        String oldStatus = assignment.getStatus();
        assignment.setStatus("REPAIRED");
        productAssignmentRepository.save(assignment);
        createLifecycleEvent(assignment, "REPAIR_COMPLETED", oldStatus, "REPAIRED", description);
        return mapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public ProductAssignmentDto markConsumed(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        String itemType = assignment.getReceivedProduct().getRequirementItem().getItemType();
        if ("ASSET".equals(itemType))
            throw new IllegalStateException("ASSETs cannot be consumed");
        if (!"ASSIGNED".equals(assignment.getStatus()))
            throw new IllegalStateException("Only ASSIGNED consumables can be consumed");

        String oldStatus = assignment.getStatus();
        assignment.setStatus("CONSUMED");
        productAssignmentRepository.save(assignment);
        createLifecycleEvent(assignment, "CONSUMED", oldStatus, "CONSUMED", description);
        return mapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public ProductAssignmentDto markNotRepairable(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        if (!"UNDER_REPAIR".equals(assignment.getStatus()))
            throw new IllegalStateException("Only assets UNDER_REPAIR can be marked NOT_REPAIRABLE");
        String oldStatus = assignment.getStatus();
        assignment.setStatus("NOT_REPAIRABLE");
        productAssignmentRepository.save(assignment);
        createLifecycleEvent(assignment, "NOT_REPAIRABLE", oldStatus, "NOT_REPAIRABLE", description);
        return mapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public ProductAssignmentDto returnAssignment(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        if ("CONSUMED".equals(assignment.getStatus()))
            throw new IllegalStateException("CONSUMED items cannot be returned");
        String oldStatus = assignment.getStatus();
        assignment.setStatus("RETURNED");
        assignment.setDeleted(true); // Return it to pool essentially
        productAssignmentRepository.save(assignment);

        ReceivedProduct rp = assignment.getReceivedProduct();
        rp.setAssignedQuantity(rp.getAssignedQuantity() - assignment.getQuantity());
        updateStatus(rp);
        receivedProductRepository.save(rp);

        createLifecycleEvent(assignment, "RETURNED", oldStatus, "RETURNED", description);
        return mapToAssignmentDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.project.www.vendor.dto.ProductLifecycleEventDto> getAssignmentHistory(Long assignmentId) {
        Long tenantId = com.project.www.util.TenantContext.getCurrentTenant();
        return productLifecycleEventRepository.findByAssignmentIdAndTenantIdOrderByCreatedAtDesc(assignmentId, tenantId)
                .stream()
                .map(e -> {
                    String performedByName = userRepository.findById(e.getPerformedBy())
                            .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");
                    String assignedToName = e.getAssignedTo() != null ? userRepository.findById(e.getAssignedTo())
                            .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown") : null;
                    return com.project.www.vendor.dto.ProductLifecycleEventDto.builder()
                            .id(e.getId())
                            .assignmentId(e.getAssignment().getId())
                            .eventType(e.getEventType())
                            .previousStatus(e.getPreviousStatus())
                            .newStatus(e.getNewStatus())
                            .performedBy(e.getPerformedBy())
                            .performedByName(performedByName)
                            .assignedTo(e.getAssignedTo())
                            .assignedToName(assignedToName)
                            .description(e.getDescription())
                            .createdAt(e.getCreatedAt())
                            .build();
                }).collect(java.util.stream.Collectors.toList());
    }

    private ProductAssignmentDto mapToAssignmentDto(ProductAssignment a) {
        String productName = "Unknown Product";
        String itemType = "ASSET";
        if (a.getReceivedProduct() != null && a.getReceivedProduct().getRequirementItem() != null) {
            productName = a.getReceivedProduct().getRequirementItem().getItemName();
            itemType = a.getReceivedProduct().getRequirementItem().getItemType();
            if (itemType == null) itemType = "ASSET";
        }

        Long userId = null;
        String userName = "Unknown User";
        if (a.getAssignedUser() != null) {
            userId = a.getAssignedUser().getId();
            String fName = a.getAssignedUser().getFirstName() != null ? a.getAssignedUser().getFirstName() : "";
            String lName = a.getAssignedUser().getLastName() != null ? a.getAssignedUser().getLastName() : "";
            userName = (fName + " " + lName).trim();
            if (userName.isEmpty()) userName = "User #" + userId;
        }

        return ProductAssignmentDto.builder()
                .id(a.getId())
                .receivedProductId(a.getReceivedProduct() != null ? a.getReceivedProduct().getId() : null)
                .productName(productName)
                .userId(userId)
                .userName(userName)
                .quantity(a.getQuantity() != null ? a.getQuantity() : 1)
                .assignedAt(a.getAssignedAt())
                .assignedBy(a.getAssignedBy())
                .status(a.getStatus() != null ? a.getStatus() : "ASSIGNED")
                .assetIdentifier(a.getAssetIdentifier())
                .itemType(itemType)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAssignmentDto> getAllAssignments(Pageable pageable) {
        Long tenantId = com.project.www.util.TenantContext.getCurrentTenant();
        return productAssignmentRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::mapToAssignmentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenant();
        return productAssignmentRepository
                .findByTenantIdAndReceivedProductIdAndDeletedFalse(tenantId, receivedProductId, pageable)
                .map(this::mapToAssignmentDto);
    }

    @Override
    @Transactional
    public ReceivedProductDto returnProduct(Long receivedProductId, Integer quantity) {
        Long tenantId = TenantContext.getCurrentTenant();
        ReceivedProduct rp = receivedProductRepository.findByIdAndTenantIdAndDeletedFalse(receivedProductId, tenantId)
                .orElseThrow(() -> new RuntimeException("Received product not found"));

        int available = rp.getReceivedQuantity() - rp.getAssignedQuantity();
        if (quantity > available) {
            throw new IllegalArgumentException(
                    "Cannot return more than the available (unassigned) quantity. Unassign first.");
        }

        rp.setReceivedQuantity(rp.getReceivedQuantity() - quantity);
        updateStatus(rp);

        if (rp.getReceivedQuantity() == 0 && rp.getAssignedQuantity() == 0) {
            rp.setDeleted(true);
        }

        receivedProductRepository.save(rp);
        return mapToDto(rp);
    }

    private void updateStatus(ReceivedProduct rp) {
        int required = rp.getRequirementItem().getQuantity();
        int received = rp.getReceivedQuantity();
        int assigned = rp.getAssignedQuantity();

        if (assigned == required) {
            rp.setStatus("FULLY_ASSIGNED");
        } else if (assigned > 0) {
            rp.setStatus("PARTIALLY_ASSIGNED");
        } else if (received == required) {
            rp.setStatus("RECEIVED");
        } else if (received > 0) {
            rp.setStatus("PARTIALLY_RECEIVED");
        } else {
            rp.setStatus("NOT_RECEIVED");
        }
    }

    private ReceivedProductDto mapToDto(ReceivedProduct rp) {
        RequirementItem item = rp.getRequirementItem();
        return ReceivedProductDto.builder()
                .id(rp.getId())
                .vendorId(rp.getVendor().getId())
                .vendorName(rp.getVendor().getVendorName())
                .requirementId(item.getRequirement().getId())
                .requirementItemId(item.getId())
                .productName(item.getItemName())
                .requiredQuantity(item.getQuantity())
                .receivedQuantity(rp.getReceivedQuantity())
                .assignedQuantity(rp.getAssignedQuantity())
                .availableQuantity(rp.getReceivedQuantity() - rp.getAssignedQuantity())
                .status(rp.getStatus())
                .build();
    }
}
