package com.project.www;

import com.project.www.accessmanagement.entity.User;
import com.project.www.accessmanagement.repository.UserRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestDebugRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        tenantRepository.findAll().forEach(t -> {
            TenantContext.setCurrentTenant(t.getId());
            TenantContext.setCurrentTenantCode(t.getCode());

            userRepository.findAllByTenantId(t.getId()).forEach(u -> {
                if ("permission@gmail.com".equals(u.getEmail())) {
                    System.out.println("=================================================");
                    System.out.println("DEBUG: Found user: " + u.getEmail() + " in Tenant: " + t.getCode());
                    if (u.getRole() != null) {
                        System.out.println("DEBUG: Role name: " + u.getRole().getName());
                        System.out.println("DEBUG: Role active: " + u.getRole().getActive());
                        System.out.println("DEBUG: Role permissions count: " + u.getRole().getPermissions().size());
                        u.getRole().getPermissions().forEach(p -> {
                            System.out.println("DEBUG: Permission: " + p.getPermissionKey() + ", Active: " + p.getActive());
                        });
                    } else {
                        System.out.println("DEBUG: User has no role assigned.");
                    }
                    System.out.println("=================================================");
                }
            });

            TenantContext.clear();
        });
    }
}
