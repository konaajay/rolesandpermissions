package com.project.www.vendor.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "vendor_complaints")
public class VendorComplaint extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false, length = 255)
    private String productOrService;       // What product/service the complaint is about

    @Column(nullable = false, length = 100)
    private String complaintType;          // Quality, Delivery, Billing, Communication, Other

    @Column(nullable = false, length = 50)
    private String severity;               // Low, Medium, High, Critical

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String status;                 // Open, In Progress, Resolved, Closed

    private String dateReported;

    private String resolvedDate;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
