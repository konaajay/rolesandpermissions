package com.project.www.security;

import com.project.www.tenant.entity.Tenant;

import com.project.www.util.TenantContext;
import com.project.www.tenant.repository.TenantRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TenantRepository tenantRepository;
    

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Always clear context at the start of a request to prevent thread contamination
        TenantContext.clear();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();

        if ((path.startsWith("/auth/")
                || path.startsWith("/api/auth/")
                || path.startsWith("/public/")
                || path.startsWith("/uploads/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/")
                || path.equals("/error"))
                && !path.equals("/api/auth/me")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;
        final Long tenantId;
        final String tenantCode;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No valid Authorization header found. Header: " + authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            email = jwtService.extractUsername(jwt);
            tenantId = jwtService.extractTenantId(jwt);
            String code = jwtService.extractTenantCode(jwt);

            if (code == null && tenantId != null) {
                // Fallback: Resolve tenant code from Master Database
                try {
                    TenantContext.setCurrentTenantCode("master");
                    Optional<Tenant> t = tenantRepository.findById(tenantId);
                    if (t.isPresent()) {
                        code = t.get().getCode();
                    }
                } finally {
                    TenantContext.clear();
                }
            }
            tenantCode = code;

            logger.info("JWT email: " + email);
            logger.info("JWT tenantId: " + tenantId);
            logger.info("JWT tenantCode: " + tenantCode);

        } catch (Exception e) {
            logger.error("Failed to parse JWT token: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid JWT signature");
            return;
        }

        try {
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Case 1: Super Admin / Master user without tenant
                if (tenantId == null || tenantCode == null) {
                    TenantContext.setCurrentTenantCode("master");

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        logger.info("Authorities: " + userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("Authentication set successfully for: " + email);
                    }

                    filterChain.doFilter(request, response);
                    return;
                }

                // Case 2: Tenant user
                boolean tenantExists;
                try {
                    TenantContext.setCurrentTenantCode("master");
                    tenantExists = tenantRepository.existsByCode(tenantCode);
                } finally {
                    TenantContext.clear();
                }

                if (tenantExists) {
                    TenantContext.setCurrentTenant(tenantId);
                    TenantContext.setCurrentTenantCode(tenantCode);

                    UserDetails userDetails = null;
                    try {
                        userDetails = userDetailsService.loadUserByUsername(email);
                    } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                        logger.warn("JWT references non-existent user: " + email);
                    }

                    if (userDetails != null && jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        logger.info("Authorities: " + userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("Authentication set successfully for: " + email);
                    }
                } else {
                    logger.warn("Skipping authentication for user " + email + " - tenant " + tenantCode + " is not registered or is inactive.");
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}