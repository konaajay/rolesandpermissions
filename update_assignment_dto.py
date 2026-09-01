with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\dto\ProductAssignmentDto.java", "r", encoding="utf-8") as f:
    content = f.read()

if "status" not in content:
    content = content.replace("private Long assignedBy;", "private Long assignedBy;\n    private String status;\n    private String assetIdentifier;\n    private String itemType;")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\dto\ProductAssignmentDto.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated ProductAssignmentDto")
