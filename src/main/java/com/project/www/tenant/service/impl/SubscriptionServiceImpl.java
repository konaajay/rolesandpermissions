package com.project.www.tenant.service.impl;

import com.project.www.tenant.dto.SubscriptionRequest;
import com.project.www.tenant.dto.SubscriptionResponse;
import com.project.www.tenant.entity.Subscription;
import com.project.www.tenant.entity.Tenant;
import com.project.www.tenant.mapper.SubscriptionMapper;
import com.project.www.tenant.repository.SubscriptionRepository;
import com.project.www.tenant.repository.TenantRepository;
import com.project.www.tenant.service.SubscriptionService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionResponse upgradeSubscription(SubscriptionRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        
        // Temporarily clear context to save against the master db properly
        String ogCode = TenantContext.getCurrentTenantCode();
        Tenant tenant;
        try {
            TenantContext.clear();
            tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
                
            // 0. Find the plan if provided
            com.project.www.tenant.entity.SubscriptionPlan plan = null;
            if (request.getPlanId() != null) {
                plan = com.project.www.tenant.repository.SubscriptionPlanRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(
                        ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext()
                ).getBean(com.project.www.tenant.repository.SubscriptionPlanRepository.class)).findById(request.getPlanId()).orElse(null);
            }
                
            LocalDate startDate = LocalDate.now();
            int days = request.getDurationDays() != null ? request.getDurationDays() : 30;
            if ("YEARLY".equalsIgnoreCase(request.getBillingInterval())) {
                days = 365;
            }
            LocalDate endDate = startDate.plusDays(days);
            
            // 1. Create the History Record
            Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .planId(plan != null ? plan.getId() : null)
                .planName(plan != null ? plan.getName() : request.getPlanName())
                .billingInterval(request.getBillingInterval() != null ? request.getBillingInterval() : "MONTHLY")
                .amount(request.getAmount() != null ? request.getAmount() : (plan != null ? plan.getMonthlyPrice() : 0.0))
                .startDate(startDate)
                .endDate(endDate)
                .status("ACTIVE")
                .paymentReference(request.getPaymentReference())
                .build();
                
            subscription = subscriptionRepository.save(subscription);
            
            // 2. Update the Tenant Entity itself
            tenant.setSubscriptionType(subscription.getPlanName());
            tenant.setSubscriptionStartDate(startDate);
            tenant.setSubscriptionEndDate(endDate);
            tenant.setStatus("ACTIVE"); // Removes them from EXPIRED/TRIAL status
            tenant.setActive(true);
            
            tenantRepository.save(tenant);
            
            // 3. Update Tenant Modules based on the plan
            if (plan != null && plan.getModules() != null) {
                com.project.www.tenant.repository.TenantModuleRepository tmRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(
                        ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext()
                ).getBean(com.project.www.tenant.repository.TenantModuleRepository.class);
                
                // Keep the CORE modules + the ones from the plan
                List<com.project.www.tenant.entity.TenantModule> existing = tmRepo.findByTenantId(tenant.getId());
                // Set all to inactive first
                for (com.project.www.tenant.entity.TenantModule tm : existing) {
                    tm.setActive(false);
                    tmRepo.save(tm);
                }
                
                for (String mod : plan.getModules()) {
                    com.project.www.tenant.entity.TenantModule tm = tmRepo.findByTenantIdAndModuleName(tenant.getId(), mod).orElse(null);
                    if (tm == null) {
                        tmRepo.save(com.project.www.tenant.entity.TenantModule.builder()
                                .tenantId(tenant.getId())
                                .moduleName(mod)
                                .active(true)
                                .build());
                    } else {
                        tm.setActive(true);
                        tmRepo.save(tm);
                    }
                }
            }
            
            return subscriptionMapper.toDto(subscription);
        } finally {
            TenantContext.setCurrentTenant(tenantId);
            TenantContext.setCurrentTenantCode(ogCode);
        }
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionHistory() {
        Long tenantId = TenantContext.getCurrentTenant();
        String ogCode = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            return subscriptionRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(subscriptionMapper::toDto)
                .collect(Collectors.toList());
        } finally {
            TenantContext.setCurrentTenant(tenantId);
            TenantContext.setCurrentTenantCode(ogCode);
        }
    }

    @Override
    public List<SubscriptionResponse> getAllSubscriptions() {
        String ogCode = TenantContext.getCurrentTenantCode();
        Long ogId = TenantContext.getCurrentTenant();
        try {
            TenantContext.clear();
            return subscriptionRepository.findAll()
                .stream()
                .map(subscriptionMapper::toDto)
                .collect(Collectors.toList());
        } finally {
            TenantContext.setCurrentTenant(ogId);
            TenantContext.setCurrentTenantCode(ogCode);
        }
    }
}
