package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoogleCalendarEventRequest {
    @NotBlank
    private String summary;
    private String description;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
    private String timeZone;
    private List<String> attendees;
    private String module;
    private Long referenceId;
    private boolean createMeetLink;
}
