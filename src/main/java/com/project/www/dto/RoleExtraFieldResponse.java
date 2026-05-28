package com.project.www.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleExtraFieldResponse {
    private Long id;
    private String fieldName;
    private String label;
    private String type;
    private boolean required;
    private List<String> options;
}
