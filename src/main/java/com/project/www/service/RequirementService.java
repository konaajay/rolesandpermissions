package com.project.www.service;

import com.project.www.dto.RequirementRequest;
import com.project.www.entity.Requirement;
import java.util.List;

public interface RequirementService {
    Requirement createRequirement(RequirementRequest request);
    Requirement updateRequirementStatus(Long id, String status);
    List<Requirement> getAllRequirements();
    List<Requirement> getRequirementsByVendor(Long vendorId);
}
