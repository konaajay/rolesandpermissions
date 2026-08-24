package com.project.www.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.project.www.integrations.dto.GoogleSheetsExportRequest;
import com.project.www.integrations.exception.CredentialMissingException;
import com.project.www.integrations.exception.IntegrationException;
import com.project.www.integrations.service.*;
import com.project.www.integrations.util.JsonUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private final GoogleOAuthService googleOAuthService;
    private final TenantIntegrationResolver tenantIntegrationResolver;
    private final IntegrationLogService logService;
    private final TenantContextService tenantContextService;
    private final RestTemplate restTemplate;

    @Override
    @Transactional("integrationTransactionManager")
    public Map<String, Object> export(GoogleSheetsExportRequest request) {
        String token = googleOAuthService.getValidAccessToken();
        if (token == null || token.isBlank()) {
            throw new CredentialMissingException("Google OAuth token required for Sheets export");
        }

        Map<String, Object> spreadsheet = Map.of(
                "properties", Map.of("title", request.getSheetTitle())
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> createResponse = restTemplate.exchange(
                    "https://sheets.googleapis.com/v4/spreadsheets",
                    HttpMethod.POST, new HttpEntity<>(spreadsheet, headers), Map.class);

            if (!createResponse.getStatusCode().is2xxSuccessful() || createResponse.getBody() == null) {
                throw new IntegrationException("Failed to create Google Sheet");
            }

            String spreadsheetId = String.valueOf(createResponse.getBody().get("spreadsheetId"));
            List<List<Object>> values = new ArrayList<>();
            if (request.getHeaders() != null) {
                values.add(new ArrayList<>(request.getHeaders()));
            }
            if (request.getRows() != null) {
                for (var row : request.getRows()) {
                    values.add(new ArrayList<>(row.values()));
                }
            }

            if (!values.isEmpty()) {
                Map<String, Object> valueBody = Map.of("values", values);
                restTemplate.exchange(
                        "https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId + "/values/A1:append?valueInputOption=RAW",
                        HttpMethod.POST, new HttpEntity<>(valueBody, headers), Map.class);
            }

            var ctx = tenantIntegrationResolver.resolveContext("GOOGLE");
            Long tenantId = tenantContextService.getCurrentTenantId();
            Map<String, Object> result = Map.of(
                    "spreadsheetId", spreadsheetId,
                    "spreadsheetUrl", "https://docs.google.com/spreadsheets/d/" + spreadsheetId
            );
            logService.log(tenantId, ctx.getTenantIntegration().getId(), "GOOGLE", "sheets_export", "export",
                    JsonUtil.toJson(request), JsonUtil.toJson(result), "SUCCESS", 200, null, 0);
            return result;
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            throw new IntegrationException("Sheets export failed: " + e.getMessage());
        }
    }
}
