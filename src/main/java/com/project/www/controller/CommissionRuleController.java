package com.project.www.controller;

import com.project.www.enums.*;

import com.project.www.entity.CommissionRule;
import com.project.www.service.CommissionRuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/commission-rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommissionRuleController {

    private final CommissionRuleService service;

    // ===== VIEW =====
    @GetMapping
    @PreAuthorize("hasAnyAuthority('COMMISSION_RULE_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<CommissionRule>> getAllRules() {
        return ResponseEntity.ok(service.getAllRules());
    }

    // ===== CREATE =====
    @PostMapping
    @PreAuthorize("hasAnyAuthority('COMMISSION_RULE_CREATE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<CommissionRule> createRule(
            @Valid @RequestBody CommissionRule rule) {

        return ResponseEntity.ok(service.createRule(rule));
    }

    // ===== UPDATE =====
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COMMISSION_RULE_UPDATE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<CommissionRule> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody CommissionRule ruleDetails) {

        return service.updateRule(id, ruleDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COMMISSION_RULE_DELETE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {

        if (service.deleteRule(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
