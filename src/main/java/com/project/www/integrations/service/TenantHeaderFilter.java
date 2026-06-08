package com.project.www.integrations.service;

import com.project.www.tenant.entity.Tenant;

import com.project.www.accessmanagement.entity.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantHeader = request.getHeader("X-Tenant-Id");
        String userHeader = request.getHeader("X-User-Id");

        try {
            if (tenantHeader != null && !tenantHeader.isBlank()) {
                TenantContextHolder.setTenantId(Long.parseLong(tenantHeader));
            } else {
                TenantContextHolder.setTenantId(1L);
            }

            if (userHeader != null && !userHeader.isBlank()) {
                TenantContextHolder.setUserId(Long.parseLong(userHeader));
            } else {
                TenantContextHolder.setUserId(1L);
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContextHolder.clear();
        }
    }
}
