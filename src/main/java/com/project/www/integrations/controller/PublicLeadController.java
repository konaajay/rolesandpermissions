package com.project.www.integrations.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/public/leads")
public class PublicLeadController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLead(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
