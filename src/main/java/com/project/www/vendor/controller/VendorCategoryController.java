package com.project.www.vendor.controller;

import com.project.www.dto.ApiResponse;
import com.project.www.vendor.dto.VendorCategoryDto;
import com.project.www.vendor.service.VendorCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/vendor-categories", "/vendor-categories"})
@RequiredArgsConstructor
public class VendorCategoryController {

    private final VendorCategoryService service;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR_CATEGORY_CREATE')")
    public ResponseEntity<ApiResponse<VendorCategoryDto>> createCategory(@Valid @RequestBody VendorCategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createCategory(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_CATEGORY_UPDATE')")
    public ResponseEntity<ApiResponse<VendorCategoryDto>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody VendorCategoryDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.updateCategory(id, dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<VendorCategoryDto>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getCategoryById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR_CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<List<VendorCategoryDto>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllCategories()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR_CATEGORY_DELETE')")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
