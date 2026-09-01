with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "r", encoding="utf-8") as f:
    content = f.read()

# Add endpoints
endpoints = """
    @PostMapping("/assignments/{assignmentId}/damage")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> reportDamage(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.reportDamage(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/repair")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> sendForRepair(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.sendForRepair(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/complete-repair")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> completeRepair(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.completeRepair(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/consume")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> markConsumed(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.markConsumed(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/not-repairable")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> markNotRepairable(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.markNotRepairable(assignmentId, request.getDescription())));
    }

    @PostMapping("/assignments/{assignmentId}/return")
    @PreAuthorize("hasAuthority('VENDOR_UPDATE')")
    public ResponseEntity<ApiResponse<ProductAssignmentDto>> returnAssignment(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody com.project.www.vendor.dto.ProductLifecycleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.returnAssignment(assignmentId, request.getDescription())));
    }

    @GetMapping("/assignments/{assignmentId}/history")
    @PreAuthorize("hasAuthority('VENDOR_VIEW')")
    public ResponseEntity<ApiResponse<java.util.List<com.project.www.vendor.dto.ProductLifecycleEventDto>>> getAssignmentHistory(
            @PathVariable("assignmentId") Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success(receivedProductService.getAssignmentHistory(assignmentId)));
    }
"""

if "reportDamage" not in content:
    content = content.replace("public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAssignments(",
                              endpoints + "\n    public ResponseEntity<ApiResponse<Page<ProductAssignmentDto>>> getAssignments(")

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\controller\ReceivedProductController.java", "w", encoding="utf-8") as f:
    f.write(content)
print("Updated ReceivedProductController")
