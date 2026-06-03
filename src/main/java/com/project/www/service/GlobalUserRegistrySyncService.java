package com.project.www.service;

import com.project.www.entity.GlobalUserRegistry;
import com.project.www.entity.User;
import com.project.www.repository.GlobalUserRegistryRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalUserRegistrySyncService {

    private final GlobalUserRegistryRepository registryRepository;

    /**
     * Synchronizes a user record to the global registry.
     * Note: Must be called with TenantContext.clear() or on a transaction mapped to rbac_db
     * if the caller is inside a tenant context.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void syncUser(User user, Long tenantId) {
        GlobalUserRegistry registry = registryRepository.findByEmail(user.getEmail())
                .orElse(GlobalUserRegistry.builder()
                        .email(user.getEmail())
                        .build());
        
        registry.setTenantId(tenantId);
        registry.setUserId(user.getId());
        registry.setActive(user.getActive());
        
        registryRepository.save(registry);
        log.debug("Synced user {} to global registry for tenant {}", user.getEmail(), tenantId);
    }

    /**
     * Removes a user from the global registry (e.g. on hard delete).
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void removeUser(String email) {
        registryRepository.findByEmail(email).ifPresent(registryRepository::delete);
        log.debug("Removed user {} from global registry", email);
    }

    /**
     * Checks if an email exists globally.
     * Note: Must be called with TenantContext.clear() before invocation.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, readOnly = true)
    public boolean existsByEmail(String email) {
        return registryRepository.existsByEmail(email);
    }

    public void syncAllTenants(java.util.List<com.project.www.entity.Tenant> tenants, com.project.www.repository.UserRepository userRepository) {
        log.info("Starting GlobalUserRegistry synchronization across {} tenants...", tenants.size());
        java.util.Map<String, java.util.List<String>> duplicateEmails = new java.util.HashMap<>();
        int usersSynced = 0;

        for (com.project.www.entity.Tenant tenant : tenants) {
            try {
                TenantContext.setCurrentTenant(tenant.getId());
                TenantContext.setCurrentTenantCode(tenant.getCode());
                java.util.List<User> users = userRepository.findAllByTenantId(tenant.getId());
                
                for (User user : users) {
                    try {
                        TenantContext.clear();
                        java.util.Optional<GlobalUserRegistry> existing = registryRepository.findByEmail(user.getEmail());
                        if (existing.isPresent() && !existing.get().getTenantId().equals(tenant.getId())) {
                            duplicateEmails.computeIfAbsent(user.getEmail(), k -> new java.util.ArrayList<>())
                                .add(tenant.getName() + " (ID: " + tenant.getId() + ")");
                            // Also add the original tenant if it's the first time we find a duplicate
                            if (duplicateEmails.get(user.getEmail()).size() == 1) {
                                duplicateEmails.get(user.getEmail()).add("Original Tenant ID: " + existing.get().getTenantId());
                            }
                        } else {
                            syncUser(user, tenant.getId());
                            usersSynced++;
                        }
                    } finally {
                        TenantContext.setCurrentTenant(tenant.getId());
                        TenantContext.setCurrentTenantCode(tenant.getCode());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to sync users for tenant {}: {}", tenant.getName(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        if (!duplicateEmails.isEmpty()) {
            log.error("\n=======================================================");
            log.error("DUPLICATE EMAIL REPORT - ACTION REQUIRED");
            log.error("The following emails exist in multiple tenant databases.");
            log.error("Email-only login requires globally unique emails.");
            log.error("=======================================================\n");
            
            duplicateEmails.forEach((email, tenantList) -> {
                log.error("Duplicate email detected: {}", email);
                for (String t : tenantList) {
                    log.error("- {}", t);
                }
                log.error("");
            });
            
            log.error("=======================================================\n");
            log.error("WARNING: Startup allowed to proceed, but email-only login may fail for these duplicate users.");
        }
        
        log.info("GlobalUserRegistry synchronization complete. Synced {} unique users.", usersSynced);
    }
}
