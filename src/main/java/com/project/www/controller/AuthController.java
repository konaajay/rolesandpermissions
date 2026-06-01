package com.project.www.controller;

import com.project.www.dto.AuthResponse;
import com.project.www.dto.LoginRequest;
import com.project.www.dto.RegisterRequest;
import com.project.www.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.project.www.service.TenantService tenantService;

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/register-company")
    public org.springframework.http.ResponseEntity<com.project.www.dto.TenantResponse> registerCompany(
            @jakarta.validation.Valid @RequestBody com.project.www.dto.CreateTenantRequest request
    ) {
        return org.springframework.http.ResponseEntity.ok(tenantService.createTenant(request));
    }
}