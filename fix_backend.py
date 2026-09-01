with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "r", encoding="utf-8") as f:
    content = f.read()
if "java.util.List" not in content:
    content = content.replace("List<ProductLifecycleEventDto>", "java.util.List<ProductLifecycleEventDto>")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\ReceivedProductService.java", "w", encoding="utf-8") as f:
        f.write(content)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "r", encoding="utf-8") as f:
    content = f.read()

# I will find the EXACT string block and move it before the GetMapping annotation.
bad_block = """    @PostMapping("/assignments/{assignmentId}/damage")"""
start_idx = content.find(bad_block)
if start_idx != -1:
    end_idx = content.find("    public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAssignments(", start_idx)
    endpoints = content[start_idx:end_idx]
    
    # Remove endpoints from their current wrong location
    content = content[:start_idx] + content[end_idx:]
    
    # Now insert before @GetMapping("/{id}/assignments")
    insert_idx = content.find("    @GetMapping(\"/{id}/assignments\")")
    content = content[:insert_idx] + endpoints + "\n" + content[insert_idx:]
    
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Fixed Controller and Service")
