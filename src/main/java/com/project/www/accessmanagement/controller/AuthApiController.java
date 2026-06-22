package com.project.www.accessmanagement.controller;

import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final com.project.www.tenant.repository.TenantModuleRepository tenantModuleRepository;

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> response = new HashMap<>();
        try {
            String email = jwtService.extractUsername(token);
            if (jwtService.isTokenValid(token, email)) {
                Long tenantId = jwtService.extractTenantId(token);
                
                User user = userRepository.findFirstByEmailAndTenantId(email, tenantId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                    
                response.put("valid", true);
                response.put("userId", user.getId());
                response.put("tenantId", tenantId);
                response.put("role", user.getRole() != null ? user.getRole().getName() : null);
            } else {
                response.put("valid", false);
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-permission")
    public ResponseEntity<Map<String, Object>> checkPermission(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String permission = request.get("permission");
        Map<String, Object> response = new HashMap<>();
        try {
            String email = jwtService.extractUsername(token);
            if (jwtService.isTokenValid(token, email)) {
                Long tenantId = jwtService.extractTenantId(token);
                
                User user = userRepository.findFirstByEmailAndTenantId(email, tenantId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                boolean hasPermission = false;
                if (user.getRole() != null && "SUPER_ADMIN".equals(user.getRole().getName())) {
                    hasPermission = true;
                } else if (user.getRole() != null) {
                    hasPermission = user.getRole().getPermissions().stream()
                        .anyMatch(p -> p.getPermissionKey().equals(permission));
                }
                
                response.put("valid", true);
                response.put("userId", user.getId());
                response.put("tenantId", tenantId);
                response.put("role", user.getRole() != null ? user.getRole().getName() : null);
                response.put("allowed", hasPermission);
            } else {
                response.put("valid", false);
                response.put("allowed", false);
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("allowed", false);
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findFirstByEmailAndTenantId(email, com.project.www.util.TenantContext.getCurrentTenant())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("name", user.getFirstName() + " " + user.getLastName());
        response.put("role", user.getRole() != null ? user.getRole().getName() : null);
        
        java.util.Set<String> allPermissions = new java.util.HashSet<>();
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().forEach(p -> allPermissions.add(p.getPermissionKey()));
        }
        if (user.getPermissions() != null) {
            user.getPermissions().forEach(p -> allPermissions.add(p.getPermissionKey()));
        }
        java.util.Set<String> allModules = new java.util.HashSet<>();
        java.util.List<com.project.www.tenant.entity.TenantModule> activeTenantModules = tenantModuleRepository.findByTenantIdAndActiveTrue(user.getTenantId());
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.Set<String> activeTenantModuleNames = activeTenantModules.stream()
                .filter(m -> m.getExpiryDate() == null || !m.getExpiryDate().isBefore(today))
                .map(com.project.www.tenant.entity.TenantModule::getModuleName)
                .collect(java.util.stream.Collectors.toSet());

        // Core modules never expire and are not billed
        activeTenantModuleNames.add("SYSTEM_ADMIN");
        activeTenantModuleNames.add("EMPLOYEE");

        if (user.getRole() != null && ("SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()) || "SYSTEM_SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()))) {
            allModules.addAll(activeTenantModuleNames);
        } else {
            if (user.getModules() != null) {
                allModules.addAll(user.getModules());
            }
            if (allModules.isEmpty()) {
                // Fallback to extracting modules from permissions
                allPermissions.forEach(p -> allModules.add(p.split("_")[0]));
            }
            // Retain only modules the tenant is actively subscribed to and not expired
            allModules.retainAll(activeTenantModuleNames);
        }
        
        // Filter out permissions for modules that are not active or have expired
        java.util.Set<String> validPermissions = allPermissions.stream()
                .filter(p -> {
                    String modulePrefix = p.split("_")[0];
                    return activeTenantModuleNames.contains(modulePrefix) || "SYSTEM".equals(modulePrefix) || "TENANT".equals(modulePrefix);
                })
                .collect(java.util.stream.Collectors.toSet());
        
        response.put("permissions", new java.util.ArrayList<>(validPermissions));
        response.put("modules", new java.util.ArrayList<>(allModules));

        return ResponseEntity.ok(response);
    }
}
