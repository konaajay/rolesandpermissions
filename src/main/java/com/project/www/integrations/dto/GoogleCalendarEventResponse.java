package com.project.www.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for a created Google Calendar event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarEventResponse {
    private String eventId;
    private String htmlLink;
    private String meetLink; // optional, present when createMeetLink is true
}
