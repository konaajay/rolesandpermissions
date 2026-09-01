with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

impl = """
    @Override
    @Transactional
    public ReceivedProductDto returnProduct(Long receivedProductId, Integer quantity) {
        Long tenantId = TenantContext.getCurrentTenant();
        ReceivedProduct rp = receivedProductRepository.findByIdAndTenantIdAndDeletedFalse(receivedProductId, tenantId)
                .orElseThrow(() -> new RuntimeException("Received product not found"));
                
        int available = rp.getReceivedQuantity() - rp.getAssignedQuantity();
        if (quantity > available) {
            throw new IllegalArgumentException("Cannot return more than the available (unassigned) quantity. Unassign first.");
        }
        
        rp.setReceivedQuantity(rp.getReceivedQuantity() - quantity);
        updateStatus(rp);
        
        if (rp.getReceivedQuantity() == 0 && rp.getAssignedQuantity() == 0) {
            rp.setDeleted(true);
        }
        
        receivedProductRepository.save(rp);
        return mapToDto(rp);
    }
"""

if "returnProduct(Long receivedProductId" not in content:
    content = content.replace(
        "private void updateStatus(ReceivedProduct rp) {",
        impl + "\n    private void updateStatus(ReceivedProduct rp) {"
    )
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated Impl")
