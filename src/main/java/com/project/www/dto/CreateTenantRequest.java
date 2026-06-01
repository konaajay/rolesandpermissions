package com.project.www.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTenantRequest {

    @com.fasterxml.jackson.annotation.JsonAlias({"tenantName", "companyName"})
    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @com.fasterxml.jackson.annotation.JsonAlias({"tenantCode", "companyCode"})
    private String tenantCode;

    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;

    @com.fasterxml.jackson.annotation.JsonAlias({"adminEmail", "email"})
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;

    @com.fasterxml.jackson.annotation.JsonAlias({"adminPassword", "password"})
    @NotBlank(message = "Admin password is required")
    private String adminPassword;

    private String phone;
}
