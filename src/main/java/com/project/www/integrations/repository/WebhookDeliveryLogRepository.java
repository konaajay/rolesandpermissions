package com.project.www.integrations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.WebhookDeliveryLog;
import com.project.www.integrations.enums.WebhookDeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookDeliveryLogRepository extends JpaRepository<WebhookDeliveryLog, Long> {

    Page<WebhookDeliveryLog> findByTenantIdAndWebhookSubscriptionIdOrderByCreatedAtDesc(
            Long tenantId, Long webhookSubscriptionId, Pageable pageable);

    List<WebhookDeliveryLog> findByStatusAndRetryCountLessThanAndNextRetryAtBefore(
            WebhookDeliveryStatus status, int maxRetry, LocalDateTime before);

    Optional<WebhookDeliveryLog> findByTenantIdAndId(Long tenantId, Long id);
}
