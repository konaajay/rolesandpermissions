package com.project.www.vendor.entity;

import com.project.www.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "received_products")
public class ReceivedProduct extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_item_id", nullable = false)
    private RequirementItem requirementItem;

    @Column(nullable = false)
    private Integer receivedQuantity;

    @Column(nullable = false)
    private Integer assignedQuantity;

    @Column(nullable = false, length = 50)
    private String status; // e.g., AVAILABLE, PARTIAL, FULLY_ASSIGNED

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
