package com.project.www.accessmanagement.dto;

import com.project.www.accessmanagement.dto.TokenRefreshRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshRequest {
    private String refreshToken;
}
