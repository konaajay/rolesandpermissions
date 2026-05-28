package com.project.www.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Dynamic permission evaluator for business-level permissions.
 *
 * Use this for permissions that:
 *  - Are tenant-defined (e.g. LEAVE_APPROVE, PAYROLL_VERIFY, BONUS_APPROVE)
 *  - Grow or change over time without code changes
 *  - Cannot be known at compile time
 *
 * Usage in @PreAuthorize:
 *   @PreAuthorize("@permissionEvaluator.hasPermission('LEAVE_APPROVE')")
 *
 * Usage in service code:
 *   if (!permissionEvaluator.hasPermission("PAYROLL_VERIFY")) {
 *       throw new AccessDeniedException("Insufficient permissions");
 *   }
 *
 * How it works:
 *   Checks the current SecurityContext authorities (already loaded from DB by
 *   CustomUserDetails.getAuthorities()) — so no extra DB query is made.
 */
@Service("permissionEvaluator")
@RequiredArgsConstructor
public class PermissionEvaluatorService {

    /**
     * Returns true if the currently authenticated user holds the given permission key.
     * The key is compared case-insensitively for safety.
     *
     * @param permissionKey e.g. "LEAVE_APPROVE"
     */
    public boolean hasPermission(String permissionKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String normalizedKey = permissionKey.toUpperCase().trim();
        return auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(normalizedKey::equals);
    }

    /**
     * Returns true if the user holds ALL of the given permission keys.
     *
     * @param permissionKeys varargs of permission keys
     */
    public boolean hasAllPermissions(String... permissionKeys) {
        for (String key : permissionKeys) {
            if (!hasPermission(key)) return false;
        }
        return true;
    }

    /**
     * Returns true if the user holds ANY of the given permission keys.
     *
     * @param permissionKeys varargs of permission keys
     */
    public boolean hasAnyPermission(String... permissionKeys) {
        for (String key : permissionKeys) {
            if (hasPermission(key)) return true;
        }
        return false;
    }
}
