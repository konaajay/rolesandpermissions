package com.project.www.service;

import com.project.www.dto.RequirementRequest;
import com.project.www.dto.RequirementResponseDto;
import com.project.www.entity.Requirement;
import java.util.List;

public interface RequirementService {
    RequirementResponseDto createRequirement(RequirementRequest request);
    RequirementResponseDto updateRequirementStatus(Long id, String status);
    List<RequirementResponseDto> getAllRequirements();
    List<RequirementResponseDto> getRequirementsByVendor(Long vendorId);
}
