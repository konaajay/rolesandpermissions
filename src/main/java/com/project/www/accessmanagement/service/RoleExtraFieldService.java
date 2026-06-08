package com.project.www.accessmanagement.service;

import com.project.www.accessmanagement.dto.RoleExtraFieldRequest;
import com.project.www.accessmanagement.dto.RoleExtraFieldResponse;
import com.project.www.accessmanagement.entity.User;

import java.util.List;
import java.util.Map;

public interface RoleExtraFieldService {

    List<RoleExtraFieldResponse> getExtraFieldsForRole(Long roleId);

    List<RoleExtraFieldResponse> getMergedExtraFieldsForRoles(List<Long> roleIds);

    RoleExtraFieldResponse createExtraField(Long roleId, RoleExtraFieldRequest request);

    RoleExtraFieldResponse updateExtraField(Long roleId, Long fieldId, RoleExtraFieldRequest request);

    void deleteExtraField(Long roleId, Long fieldId);

    void saveUserExtraFieldValues(User user, Map<String, Object> extraFieldValues);

    Map<String, Object> getUserExtraFieldValues(Long userId);
}
