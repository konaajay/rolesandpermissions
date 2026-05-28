package com.project.www.controller;

import com.project.www.entity.LeadStatus;
import com.project.www.entity.PipelineStage;
import com.project.www.repository.PipelineStageRepository;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pipeline-stages")
@RequiredArgsConstructor
public class PipelineStageController {

    private final PipelineStageRepository pipelineStageRepository;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_VIEW)")
    public List<PipelineStage> getAll() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        return pipelineStageRepository.findAllByTenantIdOrderByOrderIndexAsc(tenantId);
    }

    @GetMapping("/active")
    public List<PipelineStage> getActive() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        return pipelineStageRepository.findByTenantIdAndActiveTrueOrderByOrderIndexAsc(tenantId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public PipelineStage create(@RequestBody PipelineStage stage) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        if (stage.getStatusValue() == null || stage.getStatusValue().trim().isEmpty()) {
            throw new RuntimeException("Status value is required");
        }
        
        String statusToUse = stage.getStatusValue().trim().toUpperCase().replace(" ", "_");
        LeadStatus matched = LeadStatus.fromString(statusToUse);
        if (matched != null) {
            statusToUse = matched.name();
        }
        stage.setStatusValue(statusToUse);

        if (pipelineStageRepository.existsByTenantIdAndStatusValue(tenantId, statusToUse)) {
            throw new RuntimeException("Status value '" + statusToUse + "' already exists under this tenant");
        }

        stage.setTenantId(tenantId);
        return pipelineStageRepository.save(stage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public PipelineStage update(@PathVariable Long id, @RequestBody PipelineStage details) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        PipelineStage existing = pipelineStageRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Pipeline stage not found"));

        if (details.getStatusValue() != null && !details.getStatusValue().equalsIgnoreCase(existing.getStatusValue())) {
            String statusToUse = details.getStatusValue().trim().toUpperCase().replace(" ", "_");
            LeadStatus matched = LeadStatus.fromString(statusToUse);
            if (matched != null) {
                statusToUse = matched.name();
            }
            if (pipelineStageRepository.existsByTenantIdAndStatusValue(tenantId, statusToUse)) {
                throw new RuntimeException("Status value '" + statusToUse + "' already exists under this tenant");
            }
            existing.setStatusValue(statusToUse);
        }

        existing.setLabel(details.getLabel());
        existing.setColor(details.getColor());
        existing.setAnalyticBucket(details.getAnalyticBucket());
        existing.setOrderIndex(details.getOrderIndex());
        existing.setActive(details.isActive());

        existing.setRequireNote(details.isRequireNote());
        existing.setRequireDate(details.isRequireDate());
        existing.setCreateTask(details.isCreateTask());

        return pipelineStageRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public void delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        PipelineStage existing = pipelineStageRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Pipeline stage not found"));
        pipelineStageRepository.delete(existing);
    }

    @PatchMapping("/{id}/reorder")
    @PreAuthorize("hasAuthority(T(com.project.www.constants.CorePermissions).USER_CREATE)")
    public List<PipelineStage> reorder(@PathVariable Long id, @RequestParam String direction) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        List<PipelineStage> stages = pipelineStageRepository.findAllByTenantIdOrderByOrderIndexAsc(tenantId);
        int index = -1;
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new RuntimeException("Pipeline stage not found");
        }

        if ("UP".equalsIgnoreCase(direction) && index > 0) {
            PipelineStage current = stages.get(index);
            PipelineStage prev = stages.get(index - 1);
            int temp = current.getOrderIndex();
            current.setOrderIndex(prev.getOrderIndex());
            prev.setOrderIndex(temp);
            pipelineStageRepository.save(current);
            pipelineStageRepository.save(prev);
        } else if ("DOWN".equalsIgnoreCase(direction) && index < stages.size() - 1) {
            PipelineStage current = stages.get(index);
            PipelineStage next = stages.get(index + 1);
            int temp = current.getOrderIndex();
            current.setOrderIndex(next.getOrderIndex());
            next.setOrderIndex(temp);
            pipelineStageRepository.save(current);
            pipelineStageRepository.save(next);
        }

        return pipelineStageRepository.findAllByTenantIdOrderByOrderIndexAsc(tenantId);
    }
}
