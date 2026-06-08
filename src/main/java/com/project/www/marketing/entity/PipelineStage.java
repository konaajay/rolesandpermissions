package com.project.www.marketing.entity;

import com.project.www.entity.Auditable;

import com.project.www.entity.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pipeline_stages", indexes = {
        @Index(name = "idx_pipeline_tenant", columnList = "tenant_id"),
        @Index(name = "idx_pipeline_order", columnList = "tenant_id, order_index")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineStage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "status_value", nullable = false)
    private String statusValue;

    @Column(nullable = false)
    private String label;

    private String color;

    @Column(name = "analytic_bucket")
    private String analyticBucket;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;


    @Builder.Default
    @Column(name = "require_note")
    private boolean requireNote = false;

    @Builder.Default
    @Column(name = "require_date")
    private boolean requireDate = false;

    @Builder.Default
    @Column(name = "create_task")
    private boolean createTask = false;
}
