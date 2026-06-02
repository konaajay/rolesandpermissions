package com.project.www.repository;

import com.project.www.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
