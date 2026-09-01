with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "r", encoding="utf-8") as f:
    content = f.read()

methods = """
    ProductAssignmentDto reportDamage(Long assignmentId, String description);
    ProductAssignmentDto sendForRepair(Long assignmentId, String description);
    ProductAssignmentDto completeRepair(Long assignmentId, String description);
    ProductAssignmentDto markConsumed(Long assignmentId, String description);
    ProductAssignmentDto markNotRepairable(Long assignmentId, String description);
    ProductAssignmentDto returnAssignment(Long assignmentId, String description);
    List<ProductLifecycleEventDto> getAssignmentHistory(Long assignmentId);
"""
if "reportDamage" not in content:
    content = content.replace("ReceivedProductDto returnProduct(Long receivedProductId, Integer quantity);", "ReceivedProductDto returnProduct(Long receivedProductId, Integer quantity);\n" + methods)
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated ReceivedProductService interface")
