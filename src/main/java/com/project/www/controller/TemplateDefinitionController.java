package com.project.www.controller;

import com.project.www.entity.TemplateDefinition;
import com.project.www.service.TemplateDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateDefinitionController {

    private final TemplateDefinitionService service;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<List<TemplateDefinition>> getAllTemplates(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(service.getAllTemplates(type));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<TemplateDefinition> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTemplateById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<TemplateDefinition> createTemplate(@RequestBody TemplateDefinition template) {
        return ResponseEntity.ok(service.createTemplate(template));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<TemplateDefinition> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateDefinition template) {
        return ResponseEntity.ok(service.updateTemplate(id, template));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/system")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<List<TemplateDefinition>> getAvailableSystemTemplates() {
        return ResponseEntity.ok(service.getAvailableSystemTemplates());
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).SETTINGS_MANAGE_TEMPLATES)")
    public ResponseEntity<List<TemplateDefinition>> importSystemTemplates(@RequestBody List<String> templateCodes) {
        return ResponseEntity.ok(service.importSystemTemplates(templateCodes));
    }
}
