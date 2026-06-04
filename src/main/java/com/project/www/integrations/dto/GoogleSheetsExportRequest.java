package com.project.www.integrations.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GoogleSheetsExportRequest {
    @NotBlank
    private String sheetTitle;
    private List<String> headers;
    private List<Map<String, Object>> rows;
    private String module;
    private Long referenceId;
}
