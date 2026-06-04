package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ZoomMeetingRequest {
    @NotBlank
    private String topic;
    private String agenda;
    @NotNull
    private LocalDateTime startTime;
    private int durationMinutes = 30;
    private String timezone = "Asia/Kolkata";
    private String module;
    private Long referenceId;
}
