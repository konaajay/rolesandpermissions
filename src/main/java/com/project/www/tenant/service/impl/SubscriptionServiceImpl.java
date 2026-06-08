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
                
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(request.getDurationDays() != null ? request.getDurationDays() : 30);
            
            // 1. Create the History Record
            Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .planName(request.getPlanName())
                .amount(request.getAmount())
                .startDate(startDate)
                .endDate(endDate)
                .status("ACTIVE")
                .paymentReference(request.getPaymentReference())
                .build();
                
            subscription = subscriptionRepository.save(subscription);
            
            // 2. Update the Tenant Entity itself
            tenant.setSubscriptionType(request.getPlanName());
            tenant.setSubscriptionStartDate(startDate);
            tenant.setSubscriptionEndDate(endDate);
            tenant.setStatus("ACTIVE"); // Removes them from EXPIRED/TRIAL status
            tenant.setActive(true);
            
            tenantRepository.save(tenant);
            
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
