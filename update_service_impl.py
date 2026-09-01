with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

# Add UserRepository and ProductLifecycleEventRepository injects
# Wait, let's just do it cleanly via regex
import re

if "ProductLifecycleEventRepository" not in content:
    content = re.sub(r'(private final ProductAssignmentRepository productAssignmentRepository;)', 
                     r'\1\n    private final com.project.www.vendor.repository.ProductLifecycleEventRepository productLifecycleEventRepository;', 
                     content)

# Update assignProduct to split ASSET
assign_product_old = """        ProductAssignment assignment = ProductAssignment.builder()
                .tenantId(tenantId)
                .receivedProduct(rp)
                .assignedUser(assignedUser)
                .quantity(request.getQuantity())
                .assignedAt(LocalDateTime.now())
                .assignedBy(userId)
                .deleted(false)
                .build();

        productAssignmentRepository.save(assignment);

        return ProductAssignmentDto.builder()
                .id(assignment.getId())
                .receivedProductId(rp.getId())
                .userId(assignedUser.getId())
                .userName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                .quantity(assignment.getQuantity())
                .assignedAt(assignment.getAssignedAt())
                .assignedBy(assignment.getAssignedBy())
                .build();"""

assign_product_new = """        String itemType = rp.getRequirementItem().getItemType();
        if (itemType == null) itemType = "ASSET";

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
                if (firstAssignment == null) firstAssignment = assignment;
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
                .userId(assignedUser.getId())
                .userName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                .quantity(firstAssignment.getQuantity())
                .assignedAt(firstAssignment.getAssignedAt())
                .assignedBy(firstAssignment.getAssignedBy())
                .status(firstAssignment.getStatus())
                .itemType(itemType)
                .build();"""

content = content.replace(assign_product_old, assign_product_new)

# Add lifecycle methods
lifecycle_methods = """
    private void createLifecycleEvent(ProductAssignment assignment, String eventType, String oldStatus, String newStatus, String description) {
        com.project.www.vendor.entity.ProductLifecycleEvent event = com.project.www.vendor.entity.ProductLifecycleEvent.builder()
            .tenantId(assignment.getTenantId())
            .assignment(assignment)
            .eventType(eventType)
            .previousStatus(oldStatus)
            .newStatus(newStatus)
            .performedBy(com.project.www.accessmanagement.utils.UserContext.getCurrentUserId())
            .assignedTo(assignment.getAssignedUser().getId())
            .description(description)
            .createdAt(java.time.LocalDateTime.now())
            .build();
        productLifecycleEventRepository.save(event);
    }

    private ProductAssignment getAssignmentForTransition(Long assignmentId) {
        Long tenantId = com.project.www.accessmanagement.utils.TenantContext.getCurrentTenant();
        return productAssignmentRepository.findById(assignmentId)
            .filter(a -> a.getTenantId().equals(tenantId) && !a.getDeleted())
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Override
    @Transactional
    public ProductAssignmentDto reportDamage(Long assignmentId, String description) {
        ProductAssignment assignment = getAssignmentForTransition(assignmentId);
        if (!"ASSIGNED".equals(assignment.getStatus())) throw new IllegalStateException("Only ASSIGNED assets can be reported damaged");
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
        if (!"DAMAGED".equals(assignment.getStatus())) throw new IllegalStateException("Only DAMAGED assets can be sent for repair");
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
        if (!"UNDER_REPAIR".equals(assignment.getStatus())) throw new IllegalStateException("Asset is not under repair");
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
        if ("ASSET".equals(itemType)) throw new IllegalStateException("ASSETs cannot be consumed");
        if (!"ASSIGNED".equals(assignment.getStatus())) throw new IllegalStateException("Only ASSIGNED consumables can be consumed");
        
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
        if (!"UNDER_REPAIR".equals(assignment.getStatus())) throw new IllegalStateException("Only assets UNDER_REPAIR can be marked NOT_REPAIRABLE");
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
        if ("CONSUMED".equals(assignment.getStatus())) throw new IllegalStateException("CONSUMED items cannot be returned");
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
        Long tenantId = com.project.www.accessmanagement.utils.TenantContext.getCurrentTenant();
        return productLifecycleEventRepository.findByAssignmentIdAndTenantIdOrderByCreatedAtDesc(assignmentId, tenantId).stream()
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
        String itemType = a.getReceivedProduct().getRequirementItem().getItemType();
        if (itemType == null) itemType = "ASSET";
        return ProductAssignmentDto.builder()
                .id(a.getId())
                .receivedProductId(a.getReceivedProduct().getId())
                .userId(a.getAssignedUser().getId())
                .userName(a.getAssignedUser().getFirstName() + " " + a.getAssignedUser().getLastName())
                .quantity(a.getQuantity())
                .assignedAt(a.getAssignedAt())
                .assignedBy(a.getAssignedBy())
                .status(a.getStatus())
                .assetIdentifier(a.getAssetIdentifier())
                .itemType(itemType)
                .build();
    }
"""

if "reportDamage(" not in content:
    content = content.replace("public Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable) {", lifecycle_methods + "\n    public Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable) {")

# Update getAssignmentsForProduct mapping
map_old = """.map(a -> ProductAssignmentDto.builder()
                        .id(a.getId())
                        .receivedProductId(a.getReceivedProduct().getId())
                        .userId(a.getAssignedUser().getId())
                        .userName(a.getAssignedUser().getFirstName() + " " + a.getAssignedUser().getLastName())
                        .quantity(a.getQuantity())
                        .assignedAt(a.getAssignedAt())
                        .assignedBy(a.getAssignedBy())
                        .build());"""
map_new = ".map(this::mapToAssignmentDto);"
content = content.replace(map_old, map_new)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Updated ReceivedProductServiceImpl")
