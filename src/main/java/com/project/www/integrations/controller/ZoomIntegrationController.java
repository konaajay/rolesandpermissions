package com.project.www.integrations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.www.integrations.dto.*;
import com.project.www.integrations.service.IntegrationLogService;
import com.project.www.integrations.service.ZoomService;

import java.util.Map;

@RestController
@RequestMapping("/api/integrations/zoom")
@RequiredArgsConstructor
public class ZoomIntegrationController {

    private final ZoomService zoomService;
    private final IntegrationLogService logService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponseDto<OAuthConnectResponse>> connect() {
        return ResponseEntity.ok(ApiResponseDto.success(zoomService.buildConnectUrl()));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, @RequestParam(required = false) String state) {
        String redirect = zoomService.handleCallback(code, state);
        return ResponseEntity.status(302).header("Location", redirect).build();
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponseDto.success(zoomService.getStatus()));
    }

    @PostMapping("/meetings")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> createMeeting(
            @Valid @RequestBody ZoomMeetingRequest request) {
        return ResponseEntity.ok(ApiResponseDto.success(zoomService.createMeeting(request)));
    }

    @PatchMapping("/meetings/{meetingId}")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateMeeting(
            @PathVariable String meetingId,
            @Valid @RequestBody ZoomMeetingRequest request
    ) {
        Map<String, Object> updatedMeeting = zoomService.updateMeeting(meetingId, request);
        return ResponseEntity.ok(
                ApiResponseDto.success("Zoom meeting updated successfully", updatedMeeting)
        );
    }

    @DeleteMapping("/meetings/{meetingId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteMeeting(@PathVariable String meetingId) {
        zoomService.deleteMeeting(meetingId);
        return ResponseEntity.ok(ApiResponseDto.success("Meeting deleted", null));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponseDto<IntegrationTestResponse>> test() {
        return ResponseEntity.ok(ApiResponseDto.success(zoomService.testConnection()));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponseDto<Void>> disconnect() {
        zoomService.disconnect();
        return ResponseEntity.ok(ApiResponseDto.success("Disconnected", null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponseDto<?>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponseDto.success(
                logService.getLogs("ZOOM", PageRequest.of(page, size))));
    }
}
