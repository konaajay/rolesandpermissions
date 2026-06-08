package com.project.www.accessmanagement.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private String description;
    private Boolean active;
    private Set<String> permissions;
}
