package com.project.www.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationCardResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private String color;
    private boolean enabled;
    private boolean connected;
    private String health;
    private String lastSynced;
    private String environment;
}
