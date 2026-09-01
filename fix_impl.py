import re
with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()

# Fix imports
content = content.replace("com.project.www.accessmanagement.utils.UserContext", "com.project.www.util.UserContext")
content = content.replace("com.project.www.accessmanagement.utils.TenantContext", "com.project.www.util.TenantContext")

# Fix the broken override injection
# The block starts at `    private void createLifecycleEvent`
# And ends before `    public Page<ProductAssignmentDto> getAssignmentsForProduct`
# Wait, it actually ended right before the method signature.
# So I'll extract everything between `@Override\n` and `public Page<ProductAssignmentDto> getAssignmentsForProduct`
# and move it ABOVE `@Transactional(readOnly = true)\n    @Override`.

import sys

pattern = r'    @Transactional\(readOnly = true\)\n    @Override\n(.*?)(    public Page<ProductAssignmentDto> getAssignmentsForProduct\(Long receivedProductId, Pageable pageable\) \{)'
match = re.search(pattern, content, re.DOTALL)
if match:
    injected_methods = match.group(1)
    method_sig = match.group(2)
    # Remove from current location
    content = content.replace(match.group(0), "    @Transactional(readOnly = true)\n    @Override\n" + method_sig)
    
    # Insert ABOVE `@Transactional(readOnly = true)`
    content = content.replace("    @Transactional(readOnly = true)\n    @Override\n    public Page<ProductAssignmentDto> getAssignmentsForProduct", injected_methods + "    @Transactional(readOnly = true)\n    @Override\n    public Page<ProductAssignmentDto> getAssignmentsForProduct")
    
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed Service Impl")
else:
    print("Pattern not found")
