package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.service.*;

import java.util.Map;

@RestController
@RequestMapping("/api/integrations/google")
@RequiredArgsConstructor
public class GoogleIntegrationController {

    private final GoogleOAuthService googleOAuthService;
    private final GmailService gmailService;
    private final GoogleCalendarService googleCalendarService;
    private final GoogleDriveService googleDriveService;
    private final GoogleSheetsService googleSheetsService;
    private final IntegrationService integrationService;

    @GetMapping("/oauth/connect")
    public ResponseEntity<ApiResponseDto<OAuthConnectResponse>> connect() {
        return ResponseEntity.ok(ApiResponseDto.success(googleOAuthService.buildConnectUrl()));
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, @RequestParam(required = false) String state) {
        String redirect = googleOAuthService.handleCallback(code, state);
        return ResponseEntity.status(302).header("Location", redirect).build();
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<IntegrationDetailsResponse>> status() {
        return ResponseEntity.ok(ApiResponseDto.success(integrationService.getIntegrationDetails("GOOGLE")));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponseDto<Void>> disconnect() {
        integrationService.disconnectIntegration("GOOGLE");
        return ResponseEntity.ok(ApiResponseDto.success("Disconnected", null));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponseDto<IntegrationTestResponse>> test() {
        return ResponseEntity.ok(ApiResponseDto.success("Connection test successful", googleOAuthService.testConnection()));
    }

    @PostMapping("/gmail/send")
    public ResponseEntity<ApiResponseDto<Void>> sendGmail(@Valid @RequestBody GmailSendRequest request) {
        gmailService.sendEmail(request);
        return ResponseEntity.ok(ApiResponseDto.success("Email sent successfully", null));
    }

    @PostMapping("/calendar/events")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> createCalendarEvent(
            @Valid @RequestBody GoogleCalendarEventRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(googleCalendarService.createEvent(request)));
    }

    @PostMapping("/drive/upload")
    public ResponseEntity<ApiResponseDto<GoogleDriveUploadResponse>> uploadDrive(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long referenceId) {
        return ResponseEntity.ok(ApiResponseDto.success(googleDriveService.upload(file, module, referenceId)));
    }

    @PostMapping("/sheets/export")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> exportSheets(
            @Valid @RequestBody GoogleSheetsExportRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(googleSheetsService.export(request)));
    }
}
