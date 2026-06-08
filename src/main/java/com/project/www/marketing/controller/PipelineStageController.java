package com.project.www.marketing.controller;

import com.project.www.marketing.entity.PipelineStage;
import com.project.www.marketing.repository.PipelineStageRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pipeline-stages")
@RequiredArgsConstructor
public class PipelineStageController {

    private final PipelineStageRepository pipelineStageRepository;

    @GetMapping
    public ResponseEntity<List<PipelineStage>> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(pipelineStageRepository.findAllByTenantIdOrderByOrderIndexAsc(tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system_settings.edit') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PipelineStage> create(@RequestBody PipelineStage stage) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        stage.setTenantId(tenantId);
        return ResponseEntity.ok(pipelineStageRepository.save(stage));
    }
}
