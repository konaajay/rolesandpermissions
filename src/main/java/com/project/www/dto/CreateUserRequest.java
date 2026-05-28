package com.project.www.dto;

import com.project.www.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private Gender gender;

    private String phoneNumber;

    private String roleCode;

    private Long roleId;

    private java.util.List<Long> roleIds;

    private Long supervisorUserId;
    
    private java.util.Map<String, Object> profileData;
}
