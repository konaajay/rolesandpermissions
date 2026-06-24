package com.project.www.accessmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    private Long tenantId;

    private String tenantName;

    private String tenantCode;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Pattern(regexp = "^[\\w.+\\-]+@gmail\\.com$", message = "Email must be a Gmail address")
    private String email;

    @NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$", message = "Password must include one uppercase, one lowercase, one digit, and one special char")
    private String password;

    private Long roleId;
}
