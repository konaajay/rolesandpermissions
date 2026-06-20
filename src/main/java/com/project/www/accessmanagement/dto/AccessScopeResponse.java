package com.project.www.accessmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessScopeResponse {
    private Long loggedInUserId;
    private String role;
    private boolean canViewAll;
    private List<String> accessibleEmployeeIds;
}
