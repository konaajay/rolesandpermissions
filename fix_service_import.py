with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("List<ProductLifecycleEventDto> getAssignmentHistory", "java.util.List<com.project.www.vendor.dto.ProductLifecycleEventDto> getAssignmentHistory")
with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "w", encoding="utf-8") as f:
    f.write(content)
