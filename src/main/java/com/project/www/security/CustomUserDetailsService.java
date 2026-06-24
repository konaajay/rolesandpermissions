package com.project.www.security;

import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final com.project.www.accessmanagement.repository.PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new UsernameNotFoundException("No tenant context found");
        }

        User user = userRepository
                .findFirstByEmailAndTenantId(email, tenantId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email + " under tenant: " + tenantId));

        java.util.List<com.project.www.accessmanagement.entity.Permission> allPermissions = null;
        if (user.getRole() != null && ("SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()) || "SYSTEM_SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()))) {
            String originalCode = com.project.www.util.TenantContext.getCurrentTenantCode();
            Long originalTenant = com.project.www.util.TenantContext.getCurrentTenant();
            try {
                com.project.www.util.TenantContext.clear();
                allPermissions = permissionRepository.findAllByTenantId(tenantId).stream()
                    .filter(com.project.www.accessmanagement.entity.Permission::getActive)
                    .collect(java.util.stream.Collectors.toList());
            } finally {
                com.project.www.util.TenantContext.setCurrentTenantCode(originalCode);
                com.project.www.util.TenantContext.setCurrentTenant(originalTenant);
            }
        }

        return new CustomUserDetails(user, allPermissions);
    }
}