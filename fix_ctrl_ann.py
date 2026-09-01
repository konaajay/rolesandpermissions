import re

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "r", encoding="utf-8") as f:
    content = f.read()

# I will just clean up the mess.
# First, remove the injected returnProduct entirely to revert it.
bad_block = """    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ReceivedProductDto>> returnProduct(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity) {
        ReceivedProductDto returned = receivedProductService.returnProduct(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(returned));
    }

    """
content = content.replace(bad_block, "")

# Now inject it BEFORE the GetMapping
correct_block = """
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ReceivedProductDto>> returnProduct(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity) {
        ReceivedProductDto returned = receivedProductService.returnProduct(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(returned));
    }

    @GetMapping("/{id}/assignments")"""
    
content = content.replace("    @GetMapping(\"/{id}/assignments\")", correct_block)

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed Controller Annotations")
