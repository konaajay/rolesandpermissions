with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

# Fix UserContext import
content = content.replace("com.project.www.util.UserContext", "com.project.www.security.UserContext")

# Remove orphaned @Override above createLifecycleEvent
bad_annotations = """    @Transactional(readOnly = true)
    @Override

    private void createLifecycleEvent"""
content = content.replace(bad_annotations, "    private void createLifecycleEvent")

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Fixed Impl completely")
