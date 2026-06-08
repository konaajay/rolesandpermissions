package com.project.www.accessmanagement.dto;

import com.project.www.accessmanagement.entity.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {

    private Long tenantId;

    @NotBlank(message = "Role name is required")
    private String name;

    private String code;

    private String description;

    private Set<Long> permissionIds;
}