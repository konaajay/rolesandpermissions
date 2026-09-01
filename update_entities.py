with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\entity\RequirementItem.java", "r", encoding="utf-8") as f:
    content = f.read()
if "itemType" not in content:
    content = content.replace("private String unit;", "private String unit;\n\n    @Column(name = \"item_type\")\n    private String itemType = \"ASSET\";")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\entity\RequirementItem.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated RequirementItem")

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\entity\ProductAssignment.java", "r", encoding="utf-8") as f:
    content = f.read()
if "status" not in content:
    fields = """    @Column(nullable = false)
    private Long assignedBy; // ID of the user who made the assignment

    private String status = "ASSIGNED";

    private String assetIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_assignment_id")
    private ProductAssignment replacedByAssignment;"""
    content = content.replace("    @Column(nullable = false)\n    private Long assignedBy; // ID of the user who made the assignment", fields)
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\entity\ProductAssignment.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated ProductAssignment")
