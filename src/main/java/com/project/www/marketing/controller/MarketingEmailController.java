package com.project.www.marketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/marketing/email")
public class MarketingEmailController {

    @PostMapping("/send-bulk")
    public ResponseEntity<Map<String, Object>> sendBulkEmail(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/send-all-customers")
    public ResponseEntity<Map<String, Object>> sendToAllCustomers(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}
