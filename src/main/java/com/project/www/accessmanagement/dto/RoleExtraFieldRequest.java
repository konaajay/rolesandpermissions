package com.project.www.accessmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleExtraFieldRequest {
    private String fieldName;
    private String fieldLabel;
    private String fieldType;
    private boolean required;
    private List<String> options;
    private int displayOrder;
}
