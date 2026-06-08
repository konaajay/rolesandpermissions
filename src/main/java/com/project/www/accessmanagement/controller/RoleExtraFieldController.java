package com.project.www.accessmanagement.controller;

import com.project.www.accessmanagement.dto.RoleExtraFieldRequest;
import com.project.www.accessmanagement.dto.RoleExtraFieldResponse;
import com.project.www.accessmanagement.service.RoleExtraFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleExtraFieldController {

    private final RoleExtraFieldService roleExtraFieldService;

    @GetMapping("/{roleId}/extra-fields")
    @PreAuthorize("isAuthenticated()")
    public List<RoleExtraFieldResponse> getExtraFieldsForRole(
            @PathVariable Long roleId
    ) {
        return roleExtraFieldService.getExtraFieldsForRole(roleId);
    }

    @GetMapping("/extra-fields")
    @PreAuthorize("isAuthenticated()")
    public List<RoleExtraFieldResponse> getMergedExtraFieldsForRoles(
            @RequestParam List<Long> roleIds
    ) {
        return roleExtraFieldService.getMergedExtraFieldsForRoles(roleIds);
    }

    @PostMapping("/{roleId}/extra-fields")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_CREATE)")
    public RoleExtraFieldResponse createExtraField(
            @PathVariable Long roleId,
            @RequestBody RoleExtraFieldRequest request
    ) {
        return roleExtraFieldService.createExtraField(roleId, request);
    }

    @PutMapping("/{roleId}/extra-fields/{fieldId}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_CREATE)")
    public RoleExtraFieldResponse updateExtraField(
            @PathVariable Long roleId,
            @PathVariable Long fieldId,
            @RequestBody RoleExtraFieldRequest request
    ) {
        return roleExtraFieldService.updateExtraField(roleId, fieldId, request);
    }

    @DeleteMapping("/{roleId}/extra-fields/{fieldId}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).ROLE_CREATE)")
    public void deleteExtraField(
            @PathVariable Long roleId,
            @PathVariable Long fieldId
    ) {
        roleExtraFieldService.deleteExtraField(roleId, fieldId);
    }
}
