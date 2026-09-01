package com.project.www.vendor.entity;

import com.project.www.accessmanagement.entity.User;
import com.project.www.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_assignments")
public class ProductAssignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_product_id", nullable = false)
    private ReceivedProduct receivedProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User assignedUser;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @Column(nullable = false)
    private Long assignedBy; // ID of the user who made the assignment

    private String status = "ASSIGNED";

    private String assetIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_assignment_id")
    private ProductAssignment replacedByAssignment;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
