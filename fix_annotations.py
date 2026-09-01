with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("    public Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable) {", "    @Override\n    @Transactional(readOnly = true)\n    public Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable) {")

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Fixed getAssignmentsForProduct annotations")
