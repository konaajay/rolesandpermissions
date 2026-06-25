package com.project.www;

import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.repository.TenantModuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DebugRunner implements CommandLineRunner {
    private final TenantModuleRepository repo;

    public DebugRunner(TenantModuleRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DEBUG RUNNER START");
        List<TenantModule> modules = repo.findByTenantId(2L);
        System.out.println("MODULES FOR TENANT 2: " + modules.size());
        for (TenantModule m : modules) {
            System.out.println(" - " + m.getModuleName() + " (Active: " + m.getActive() + ")");
        }
        System.out.println("DEBUG RUNNER END");
    }
}
