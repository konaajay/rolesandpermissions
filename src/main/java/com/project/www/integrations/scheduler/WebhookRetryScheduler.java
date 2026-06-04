package com.project.www.integrations.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.www.integrations.config.IntegrationProperties;
import com.project.www.integrations.entity.WebhookDeliveryLog;
import com.project.www.integrations.enums.WebhookDeliveryStatus;
import com.project.www.integrations.repository.WebhookDeliveryLogRepository;
import com.project.www.integrations.service.WebhookService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRetryScheduler {

    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final WebhookService webhookService;
    private final IntegrationProperties integrationProperties;

    @Scheduled(fixedRate = 300000)
    public void retryFailedDeliveries() {
        int maxRetry = integrationProperties.getWebhook().getMaxRetry();
        List<WebhookDeliveryLog> pending = deliveryLogRepository
                .findByStatusAndRetryCountLessThanAndNextRetryAtBefore(
                        WebhookDeliveryStatus.FAILED, maxRetry, LocalDateTime.now());

        for (WebhookDeliveryLog deliveryLog : pending) {
            try {
                webhookService.retry(deliveryLog.getWebhookSubscriptionId(), deliveryLog.getId());
            } catch (Exception e) {
                deliveryLog.setRetryCount(deliveryLog.getRetryCount() + 1);
                if (deliveryLog.getRetryCount() >= maxRetry) {
                    deliveryLog.setStatus(WebhookDeliveryStatus.FAILED);
                }
                deliveryLog.setNextRetryAt(LocalDateTime.now().plusMinutes(
                        integrationProperties.getWebhook().getRetryDelayMinutes()));
                deliveryLogRepository.save(deliveryLog);
            }
        }
    }
}
