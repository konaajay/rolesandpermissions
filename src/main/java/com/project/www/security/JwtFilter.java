package com.project.www.security;

import com.project.www.util.TenantContext;
import com.project.www.repository.TenantRepository;

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

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;
        final Long tenantId;
        final String tenantCode;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
                    Optional<com.project.www.entity.Tenant> t = tenantRepository.findById(tenantId);
                    if (t.isPresent()) {
                        code = t.get().getCode();
                    }
                } finally {
                    TenantContext.clear();
                }
            }
            tenantCode = code;
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (email != null && tenantId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (tenantCode != null && tenantRepository.existsByCode(tenantCode)) {
                    TenantContext.setCurrentTenant(tenantId);
                    TenantContext.setCurrentTenantCode(tenantCode);
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

                        SecurityContextHolder.getContext().setAuthentication(authToken);
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