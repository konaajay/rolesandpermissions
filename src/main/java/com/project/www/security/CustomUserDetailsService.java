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

        return new CustomUserDetails(user);
    }
}