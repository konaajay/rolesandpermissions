with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "r", encoding="utf-8") as f:
    content = f.read()

if "returnProduct" not in content:
    content = content.replace(
        "Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable);",
        "Page<ProductAssignmentDto> getAssignmentsForProduct(Long receivedProductId, Pageable pageable);\n    ReceivedProductDto returnProduct(Long receivedProductId, Integer quantity);"
    )
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated Interface")
