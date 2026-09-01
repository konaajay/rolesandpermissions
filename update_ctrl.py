with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "r", encoding="utf-8") as f:
    content = f.read()

ctrl = """
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ReceivedProductDto>> returnProduct(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity) {
        ReceivedProductDto returned = receivedProductService.returnProduct(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(returned));
    }
"""

if "returnProduct" not in content:
    content = content.replace(
        "public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAssignments(",
        ctrl + "\n    public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAssignments("
    )
    with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "w", encoding="utf-8") as f:
        f.write(content)
print("Updated Controller")
