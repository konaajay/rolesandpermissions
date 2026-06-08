package com.project.www.vendor.service;

import com.project.www.vendor.dto.VendorCategoryDto;
import java.util.List;

public interface VendorCategoryService {
    VendorCategoryDto createCategory(VendorCategoryDto dto);
    VendorCategoryDto updateCategory(Long id, VendorCategoryDto dto);
    VendorCategoryDto getCategoryById(Long id);
    List<VendorCategoryDto> getAllCategories();
    void deleteCategory(Long id);
}
