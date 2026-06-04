package com.project.www.integrations.service;

import java.util.List;
import java.util.Map;

import com.project.www.integrations.dto.MetaConfigureRequest;

public interface MetaService {
    void configure(MetaConfigureRequest request);
    Map<String, Object> getStatus();
    List<Map<String, Object>> getPages();
    List<Map<String, Object>> getForms();
    Map<String, Object> syncLeads();
    void handleWebhook(String payload, String signature);
}
