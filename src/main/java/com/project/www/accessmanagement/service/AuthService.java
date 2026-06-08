package com.project.www.accessmanagement.service;

import com.project.www.accessmanagement.dto.LoginRequest;
import com.project.www.accessmanagement.dto.RegisterRequest;
import com.project.www.accessmanagement.dto.AuthResponse;


import com.project.www.accessmanagement.service.AuthService;

import com.project.www.accessmanagement.dto.AuthResponse;
import com.project.www.accessmanagement.dto.LoginRequest;
import com.project.www.accessmanagement.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}