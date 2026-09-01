with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "r", encoding="utf-8") as f:
    content = f.read()

new_mapping = """    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAllAssignments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.getAllAssignments(pageable)));
    }
"""

if "@GetMapping(\"/assignments\")" not in content:
    content = content.replace("    @GetMapping(\"/{id}/assignments\")", new_mapping + "\n    @GetMapping(\"/{id}/assignments\")")
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Added /assignments mapping")
