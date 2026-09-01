package com.project.www.vendor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_lifecycle_events")
public class ProductLifecycleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ProductAssignment assignment;

    @Column(nullable = false)
    private String eventType;

    private String previousStatus;
    
    @Column(nullable = false)
    private String newStatus;

    @Column(nullable = false)
    private Long performedBy;

    private Long assignedTo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
