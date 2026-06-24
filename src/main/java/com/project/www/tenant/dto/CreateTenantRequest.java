package com.project.www.tenant.dto;

import com.project.www.tenant.entity.Tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTenantRequest {

    @com.fasterxml.jackson.annotation.JsonAlias({"tenantName", "companyName"})
    @NotBlank(message = "Tenant name is required")
    @Size(min = 3, message = "Tenant name must be at least 3 characters")
    private String tenantName;

    @com.fasterxml.jackson.annotation.JsonAlias({"tenantCode", "companyCode"})
    @NotBlank(message = "Tenant code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Tenant code must use only A-Z, 0-9 or underscore")
    private String tenantCode;

    @com.fasterxml.jackson.annotation.JsonAlias({"domain", "customDomain"})
    private String domain;

    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;

    @com.fasterxml.jackson.annotation.JsonAlias({"adminEmail", "email"})
    @NotBlank(message = "Admin email is required")
    @Email(message = "Must be a valid email address")
    @Pattern(regexp = "^[\\w.+\\-]+@gmail\\.com$", message = "Email must be a Gmail address")
    private String adminEmail;

    @com.fasterxml.jackson.annotation.JsonAlias({"adminPassword", "password"})
    @NotBlank(message = "Admin password is required")
    private String adminPassword;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Database name can only contain letters, digits, or underscore")
    private String databaseName;
}
