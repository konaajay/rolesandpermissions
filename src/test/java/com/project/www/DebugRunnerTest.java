package com.project.www;

import com.project.www.tenant.entity.TenantModule;
import com.project.www.tenant.repository.TenantModuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.project.www.util.TenantContext;

import java.util.List;

@SpringBootTest
public class DebugRunnerTest {

    @Autowired
    private TenantModuleRepository repo;

    @Autowired
    private com.project.www.tenant.service.TenantModuleService tenantModuleService;
    
    @Test
    public void testGetModules() {
        TenantContext.clear();
        System.out.println("DEBUG RUNNER START");
        
        System.out.println("Testing saveBulkModules...");
        com.project.www.tenant.dto.BulkModuleSaveRequest req = new com.project.www.tenant.dto.BulkModuleSaveRequest();
        req.setPaymentType("FULL");
        req.setInvoiceType("NEW_SUBSCRIPTION");
        req.setGstPercentage(18.0);
        
        com.project.www.tenant.dto.BulkModuleItemRequest item = new com.project.www.tenant.dto.BulkModuleItemRequest();
        item.setModuleName("VENDOR");
        item.setAmount(5000.0);
        item.setExtraCharges(0.0);
        item.setStartDate(java.time.LocalDate.now());
        item.setExpiryDate(java.time.LocalDate.now().plusYears(1));
        
        req.setModules(java.util.Collections.singletonList(item));
        
        tenantModuleService.saveBulkModules(2L, req);
        
        List<TenantModule> modules = repo.findByTenantId(2L);
        System.out.println("MODULES FOR TENANT 2 AFTER SAVE: " + modules.size());
        for (TenantModule m : modules) {
            System.out.println(" - " + m.getModuleName() + " (Active: " + m.getActive() + ")");
        }
        System.out.println("DEBUG RUNNER END");
    }
}
