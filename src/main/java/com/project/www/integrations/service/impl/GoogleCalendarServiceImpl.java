package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.GoogleCalendarEventRequest;
import com.project.www.integrations.entity.ExternalEventMapping;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.repository.ExternalEventMappingRepository;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private static final String CALENDAR_EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private final GoogleOAuthService googleOAuthService;
    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationLogService logService;
    private final TenantContextService tenantContextService;
    private final ExternalEventMappingRepository eventMappingRepository;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public Map<String, Object> createEvent(GoogleCalendarEventRequest request) {
        String token = googleOAuthService.getValidAccessToken();
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Google OAuth token required for Calendar");
        }

        Map<String, Object> event = new HashMap<>();
        event.put("summary", request.getSummary());
        event.put("description", request.getDescription());
        event.put("start", Map.of("dateTime", request.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "timeZone", request.getTimeZone() != null ? request.getTimeZone() : "Asia/Kolkata"));
        event.put("end", Map.of("dateTime", request.getEndDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "timeZone", request.getTimeZone() != null ? request.getTimeZone() : "Asia/Kolkata"));

        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            List<Map<String, String>> attendees = request.getAttendees().stream()
                    .map(email -> Map.of("email", email))
                    .toList();
            event.put("attendees", attendees);
        }

        if (request.isCreateMeetLink()) {
            event.put("conferenceData", Map.of(
                    "createRequest", Map.of(
                            "requestId", UUID.randomUUID().toString(),
                            "conferenceSolutionKey", Map.of("type", "hangoutsMeet")
                    )
            ));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = CALENDAR_EVENTS_URL + (request.isCreateMeetLink() ? "?conferenceDataVersion=1" : "");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(event, headers), Map.class);

            var ctx = tenantIntegrationResolver.resolveContext("GOOGLE");
            Long tenantId = tenantContextService.getCurrentTenantId();

            if (response.getBody() != null && response.getBody().get("id") != null) {
                eventMappingRepository.save(ExternalEventMapping.builder()
                        .tenantId(tenantId)
                        .provider("GOOGLE_CALENDAR")
                        .externalEventId(String.valueOf(response.getBody().get("id")))
                        .internalModule(request.getModule())
                        .internalReferenceId(request.getReferenceId())
                        .metadataJson(JsonUtil.toJson(response.getBody()))
                        .build());
            }

            logService.log(tenantId, ctx.getTenantIntegration().getId(), "GOOGLE", "calendar_event", "create",
                    JsonUtil.toJson(request), JsonUtil.toJson(response.getBody()),
                    response.getStatusCode().is2xxSuccessful() ? "SUCCESS" : "FAILED",
                    response.getStatusCode().value(), null, 0);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IntegrationException("Google Calendar API error");
            }
            return response.getBody();
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            throw new IntegrationException("Failed to create calendar event: " + e.getMessage());
        }
    }
}
