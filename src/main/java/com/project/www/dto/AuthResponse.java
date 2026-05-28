package com.project.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private Set<String> permissions;
    private Set<String> modules;

    public AuthResponse(String token, Set<String> permissions, Set<String> modules) {
        this.token = token;
        this.permissions = permissions;
        this.modules = modules;
    }
}