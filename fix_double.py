with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

bad_annotations = """    @Transactional(readOnly = true)
    @Override

    @Override
    @Transactional(readOnly = true)
    public Page<ProductAssignmentDto> getAllAssignments"""

content = content.replace(bad_annotations, """    @Override
    @Transactional(readOnly = true)
    public Page<ProductAssignmentDto> getAllAssignments""")

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Removed double annotations")
