package com.project.www.accessmanagement.dto;

import com.project.www.entity.Gender;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private Long tenantId;
    
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private String phoneNumber;

    private java.util.Map<String, Object> profileData;

    private Boolean active;

    private Long roleId;
    private String roleName;
    private java.util.List<Long> roleIds;
    private java.util.List<String> roleNames;

    private Long supervisorUserId;
    private String supervisorName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private java.util.List<String> modules;
    private java.util.List<String> permissions;
    private java.util.List<Long> permissionIds;

    private String employeeId;
    private java.time.LocalDate dateOfBirth;
    private java.time.LocalDate joiningDate;

    private Long employeeTypeId;
    private String employeeTypeName;

    private Long designationId;
    private String designationName;

    private Long workModeId;
    private String workModeName;

    private java.util.List<Long> entityIds;
    private java.util.List<String> entityNames;
    private java.util.List<Long> departmentIds;
    private java.util.List<String> departmentNames;
}
