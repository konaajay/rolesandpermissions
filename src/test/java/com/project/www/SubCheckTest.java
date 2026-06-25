package com.project.www;

import com.project.www.tenant.repository.SubscriptionRepository;
import com.project.www.util.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.project.www.tenant.entity.Subscription;

import java.util.List;

@SpringBootTest
public class SubCheckTest {

    @Autowired
    private SubscriptionRepository repo;

    @Test
    public void testGetSubs() {
        TenantContext.clear();
        System.out.println("DEBUG RUNNER START");
        
        List<Subscription> subs = repo.findAll();
        System.out.println("ALL SUBS: " + subs.size());
        for (Subscription s : subs) {
            System.out.println(" - Tenant ID: " + (s.getTenant() != null ? s.getTenant().getId() : "null") + " Plan: " + s.getPlanName());
        }
        System.out.println("DEBUG RUNNER END");
    }
}
