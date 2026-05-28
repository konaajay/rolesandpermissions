package com.project.www.service;

import com.project.www.dto.AuthResponse;
import com.project.www.dto.LoginRequest;
import com.project.www.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}