package com.project.www.integrations.service;

import java.util.Map;

import com.project.www.integrations.dto.GoogleSheetsExportRequest;

public interface GoogleSheetsService {
    Map<String, Object> export(GoogleSheetsExportRequest request);
}
