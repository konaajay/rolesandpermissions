package com.project.www.integrations.event;

import java.util.Map;

/**
 * Adapter hook for CRM Lead module.
 * TODO: Wire to existing LeadService when CRM module is available.
 */
public interface LeadIntegrationAdapter {

    Long createLeadFromExternalPayload(String source, Map<String, Object> payload);
}
