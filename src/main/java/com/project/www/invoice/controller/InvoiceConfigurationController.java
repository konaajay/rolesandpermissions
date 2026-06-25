package com.project.www.invoice.controller;

import com.project.www.invoice.dto.InvoiceConfigurationDto;
import com.project.www.invoice.service.InvoiceConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-configurations")
@RequiredArgsConstructor
public class InvoiceConfigurationController {

    private final InvoiceConfigurationService service;

    @PostMapping
    public ResponseEntity<InvoiceConfigurationDto> createConfiguration(@Valid @RequestBody InvoiceConfigurationDto dto) {
        return ResponseEntity.ok(service.createConfiguration(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceConfigurationDto> updateConfiguration(@PathVariable Long id, @Valid @RequestBody InvoiceConfigurationDto dto) {
        return ResponseEntity.ok(service.updateConfiguration(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceConfigurationDto> getConfiguration(@PathVariable Long id) {
        return ResponseEntity.ok(service.getConfigurationById(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceConfigurationDto>> getAllConfigurations() {
        return ResponseEntity.ok(service.getAllConfigurationsForTenant());
    }

    @GetMapping("/active")
    public ResponseEntity<InvoiceConfigurationDto> getActiveConfiguration() {
        return ResponseEntity.ok(service.getActiveConfigurationForTenant());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateConfiguration(@PathVariable Long id) {
        service.activateConfiguration(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long id) {
        service.deleteConfiguration(id);
        return ResponseEntity.noContent().build();
    }
}
