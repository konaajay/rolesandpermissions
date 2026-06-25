package com.project.www;

import com.project.www.tenant.repository.TenantModuleRepository;
import com.project.www.tenant.entity.TenantModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CheckModulesTest {

    @Autowired
    private TenantModuleRepository repo;

    @Test
    public void testModules() {
        System.out.println("DEBUG RUNNER START");
        List<TenantModule> modules = repo.findAll();
        System.out.println("Total tenant_modules: " + modules.size());
        for(TenantModule tm : modules) {
            System.out.println("Module: " + tm.getModuleName() + " TenantId: " + tm.getTenantId());
        }
        System.out.println("DEBUG RUNNER END");
    }
}
