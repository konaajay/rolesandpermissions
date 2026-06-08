package com.project.www.accessmanagement.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleHierarchyResponse {
    private Long id;
    private Long roleId;
    private String roleName;
    private String roleCode;
    private Long reportsToRoleId;
    private String reportsToRoleName;
    private String reportsToRoleCode;
}
