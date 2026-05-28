package com.project.www.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private Long id;
    private Long tenantId;
    private String module;
    private String action;
    private String permissionKey;
    private String description;
    private Boolean active;
}
