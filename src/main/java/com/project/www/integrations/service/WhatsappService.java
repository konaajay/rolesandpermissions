package com.project.www.integrations.service;

import java.util.Map;

import com.project.www.integrations.dto.IntegrationTestResponse;
import com.project.www.integrations.dto.WhatsappConfigureRequest;
import com.project.www.integrations.dto.WhatsappSendRequest;

public interface WhatsappService {
    void configure(WhatsappConfigureRequest request);
    Map<String, Object> getStatus();
    void sendMessage(WhatsappSendRequest request);
    IntegrationTestResponse testConnection();
    void disconnect();
}
