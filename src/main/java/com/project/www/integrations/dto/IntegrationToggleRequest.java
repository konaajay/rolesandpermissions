package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IntegrationToggleRequest {
    @NotNull
    private Boolean enabled;
}
