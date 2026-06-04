package com.project.www.integrations.service;

import java.util.Map;

import com.project.www.integrations.dto.GoogleCalendarEventRequest;

public interface GoogleCalendarService {
    Map<String, Object> createEvent(GoogleCalendarEventRequest request);
}
