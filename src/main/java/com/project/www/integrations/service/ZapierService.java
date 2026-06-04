package com.project.www.integrations.service;

import java.util.List;
import java.util.Map;

import com.project.www.integrations.dto.ZapierWebhookConfigureRequest;

public interface ZapierService {
    Map<String, String> generateApiKey();
    Map<String, String> regenerateApiKey();
    void revokeApiKey();
    List<String> getTriggers();
    Map<String, Object> getSampleLead();
    void configureWebhook(ZapierWebhookConfigureRequest request);
    boolean sendEvent(String eventName, Object payload);
}
