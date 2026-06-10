package com.project.www.security;

import com.project.www.accessmanagement.entity.User;


import com.project.www.accessmanagement.entity.Permission;
import com.project.www.accessmanagement.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        java.util.Set<GrantedAuthority> authorities = new java.util.HashSet<>();

        if (user.getRole() != null && user.getRole().getActive()) {
            user.getRole().getPermissions().stream()
                    .filter(p -> p != null && p.getActive() && p.getPermissionKey() != null)
                    .map(p -> p.getPermissionKey().toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);

            if (user.getRole().getName() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
            }
        }

        if (user.getPermissions() != null) {
            user.getPermissions().stream()
                    .filter(p -> p != null && p.getActive() && p.getPermissionKey() != null)
                    .map(p -> p.getPermissionKey().toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getActive();
    }

    public Long getTenantId() {
        return user.getTenantId();
    }

    public User getUser() {
        return user;
    }
}