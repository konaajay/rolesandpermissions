package com.project.www.vendor.service;

import com.project.www.vendor.dto.RequirementRequest;
import com.project.www.vendor.dto.RequirementResponseDto;
import com.project.www.vendor.entity.Requirement;
import java.util.List;

public interface RequirementService {
    RequirementResponseDto createRequirement(RequirementRequest request);
    RequirementResponseDto updateRequirementStatus(Long id, String status);
    List<RequirementResponseDto> getAllRequirements();
    List<RequirementResponseDto> getRequirementsByVendor(Long vendorId);
}
