package com.project.www.accessmanagement.service;

import com.project.www.tenant.entity.Tenant;

import com.project.www.accessmanagement.repository.UserRepository;

import com.project.www.accessmanagement.entity.GlobalUserRegistry;
import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.repository.GlobalUserRegistryRepository;
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
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    /**
     * Synchronizes a user record to the global registry.
     */
    public void syncUser(User user, Long tenantId) {
        String ogCode = TenantContext.getCurrentTenantCode();
        Long ogId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            org.springframework.transaction.support.TransactionTemplate template = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
            template.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.executeWithoutResult(status -> {
                GlobalUserRegistry registry = registryRepository.findByEmailAndTenantCode(user.getEmail(), ogCode)
                        .orElse(GlobalUserRegistry.builder()
                                .email(user.getEmail())
                                .build());
                
                registry.setTenantId(tenantId);
                registry.setTenantCode(ogCode);
                registry.setUserId(user.getId());
                registry.setActive(user.getActive());
                
                registryRepository.save(registry);
            });
            log.debug("Synced user {} to global registry for tenant {}", user.getEmail(), tenantId);
        } finally {
            TenantContext.setCurrentTenant(ogId);
            TenantContext.setCurrentTenantCode(ogCode);
        }
    }

    /**
     * Removes a user from the global registry (e.g. on hard delete).
     */
    public void removeUser(String email) {
        String ogCode = TenantContext.getCurrentTenantCode();
        Long ogId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            org.springframework.transaction.support.TransactionTemplate template = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
            template.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.executeWithoutResult(status -> {
                registryRepository.findByEmailAndTenantCode(email, ogCode).ifPresent(registryRepository::delete);
            });
            log.debug("Removed user {} from global registry", email);
        } finally {
            TenantContext.setCurrentTenant(ogId);
            TenantContext.setCurrentTenantCode(ogCode);
        }
    }

    /**
     * Checks if an email exists globally.
     */
    public boolean existsByEmail(String email) {
        String ogCode = TenantContext.getCurrentTenantCode();
        Long ogId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            org.springframework.transaction.support.TransactionTemplate template = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
            template.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.setReadOnly(true);
            return template.execute(status -> registryRepository.existsByEmail(email));
        } finally {
            TenantContext.setCurrentTenant(ogId);
            TenantContext.setCurrentTenantCode(ogCode);
        }
    }

    public void syncAllTenants(java.util.List<com.project.www.tenant.entity.Tenant> tenants, com.project.www.accessmanagement.repository.UserRepository userRepository) {
        log.info("Starting GlobalUserRegistry synchronization across {} tenants...", tenants.size());
        java.util.Map<String, java.util.List<String>> duplicateEmails = new java.util.HashMap<>();
        int usersSynced = 0;

        for (com.project.www.tenant.entity.Tenant tenant : tenants) {
            try {
                TenantContext.setCurrentTenant(tenant.getId());
                TenantContext.setCurrentTenantCode(tenant.getCode());
                java.util.List<User> users = userRepository.findAllByTenantId(tenant.getId());
                
                for (User user : users) {
                    try {
                        TenantContext.clear();
                        java.util.Optional<GlobalUserRegistry> existing = registryRepository.findByEmailAndTenantCode(user.getEmail(), tenant.getCode());
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
