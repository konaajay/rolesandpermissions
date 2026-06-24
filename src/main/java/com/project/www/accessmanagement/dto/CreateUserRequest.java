package com.project.www.accessmanagement.dto;

import com.project.www.accessmanagement.dto.CreateUserRequest;

import com.project.www.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    private Long tenantId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Pattern(regexp = "^[\\w.+\\-]+@gmail\\.com$", message = "Email must be a Gmail address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$", message = "Password must include one uppercase, one lowercase, one digit, and one special char")
    private String password;

    private Gender gender;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    private String roleCode;

    private Long roleId;

    private java.util.List<Long> roleIds;

    private Long supervisorUserId;
    
    private java.util.Map<String, Object> profileData;

    private java.util.List<String> modules;

    private java.util.List<Long> permissionIds;

    private String employeeId;
    private java.time.LocalDate dateOfBirth;
    private java.time.LocalDate joiningDate;
    private Long employeeTypeId;
    private Long designationId;
    private Long workModeId;

    private java.util.List<Long> entityIds;
    private java.util.List<Long> departmentIds;
}
