package com.project.www.vendor.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductLifecycleEventDto {
    private Long id;
    private Long assignmentId;
    private String eventType;
    private String previousStatus;
    private String newStatus;
    private Long performedBy;
    private String performedByName;
    private Long assignedTo;
    private String assignedToName;
    private String description;
    private LocalDateTime createdAt;
}
