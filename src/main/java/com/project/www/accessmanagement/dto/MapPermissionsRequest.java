package com.project.www.accessmanagement.dto;

import lombok.Data;
import java.util.Set;

@Data
public class MapPermissionsRequest {
    private Set<Long> permissionIds;
}
