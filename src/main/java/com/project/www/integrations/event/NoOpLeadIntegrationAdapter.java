package com.project.www.integrations.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class NoOpLeadIntegrationAdapter implements LeadIntegrationAdapter {

    @Override
    public Long createLeadFromExternalPayload(String source, Map<String, Object> payload) {
        log.info("LeadIntegrationAdapter: Meta/external lead received from {} - payload logged until CRM LeadService is wired", source);
        return null;
    }
}
