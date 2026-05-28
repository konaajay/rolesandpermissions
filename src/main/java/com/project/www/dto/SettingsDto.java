package com.project.www.dto;

import lombok.Data;

@Data
public class SettingsDto {
    private String employeeIdFormat;
    private String leadIdFormat;
    private Long employeeSequence;
    private Long leadSequence;
}
