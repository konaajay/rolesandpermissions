package com.project.www.integrations.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.project.www.integrations.dto.ApiResponseDto;
import com.project.www.integrations.entity.ApiKey;
import com.project.www.integrations.service.ApiKeyService;
import com.project.www.integrations.service.TenantContextHolder;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    private final ApiKeyService apiKeyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-Key");
        String apiSecret = request.getHeader("X-API-Secret");
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = request.getRemoteAddr();

        // Determine permission based on endpoint and method
        String permission = null;
        if ("POST".equalsIgnoreCase(method) && endpoint.startsWith("/api/public/leads")) {
            permission = "LEAD_CREATE";
        } else if ("GET".equalsIgnoreCase(method) && endpoint.startsWith("/api/public/leads")) {
            permission = "LEAD_VIEW";
        } else if ("GET".equalsIgnoreCase(method) && endpoint.startsWith("/api/public/payments")) {
            permission = "PAYMENT_VIEW";
        }

        try {
            ApiKey validKey = apiKeyService.validateExternalApiKey(apiKey, apiSecret, permission, endpoint, method, ipAddress);
            // Set tenant context for downstream handling
            TenantContextHolder.setTenantId(validKey.getTenantId());
            return true;
        } catch (Exception ex) {
            // Log optional details if needed (omitted for brevity)
            ApiResponseDto<Map<String, Object>> failure = ApiResponseDto.failure("Unauthorized", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(com.project.www.integrations.util.JsonUtil.toJson(failure));
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContextHolder.clear();
    }
}
