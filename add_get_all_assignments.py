with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "r", encoding="utf-8") as f:
    content = f.read()

if "getAllAssignments(" not in content:
    content = content.replace("Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable);", "Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable);\n    Page<ProductAssignmentDto> getAllAssignments(Pageable pageable);")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "w", encoding="utf-8") as f:
        f.write(content)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

impl = """
    @Override
    @Transactional(readOnly = true)
    public Page<ProductAssignmentDto> getAllAssignments(Pageable pageable) {
        Long tenantId = com.project.www.util.TenantContext.getCurrentTenant();
        return productAssignmentRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::mapToAssignmentDto);
    }
"""

if "getAllAssignments(" not in content:
    content = content.replace("public Page<ProductAssignmentDto> getAssignmentsForProduct(", impl + "\n    public Page<ProductAssignmentDto> getAssignmentsForProduct(")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
        f.write(content)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\repository\ProductAssignmentRepository.java", "r", encoding="utf-8") as f:
    content = f.read()

if "findByTenantIdAndDeletedFalse" not in content:
    content = content.replace("Page<ProductAssignment> findByTenantIdAndReceivedProductIdAndDeletedFalse", "Page<ProductAssignment> findByTenantIdAndDeletedFalse(Long tenantId, Pageable pageable);\n    Page<ProductAssignment> findByTenantIdAndReceivedProductIdAndDeletedFalse")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\repository\ProductAssignmentRepository.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated getAllAssignments logic")
