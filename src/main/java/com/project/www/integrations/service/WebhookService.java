package com.project.www.integrations.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.www.integrations.dto.WebhookDeliveryLogResponse;
import com.project.www.integrations.dto.WebhookSubscriptionRequest;
import com.project.www.integrations.dto.WebhookSubscriptionResponse;

import java.util.Map;

public interface WebhookService {
    WebhookSubscriptionResponse create(WebhookSubscriptionRequest request);
    java.util.List<WebhookSubscriptionResponse> list();
    WebhookSubscriptionResponse update(Long id, WebhookSubscriptionRequest request);
    void delete(Long id);
    void test(Long id);
    Page<WebhookDeliveryLogResponse> getLogs(Long id, Pageable pageable);
    void retry(Long subscriptionId, Long logId);
    void deliverEvent(String eventName, Map<String, Object> payload, String module, Long referenceId);
}
