package com.project.www.vendor.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vendor_audits")
public class VendorAudit extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 50)
    private String status;

    private String auditDate;

    private String nextAudit;

    @Column(length = 255)
    private String auditor;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
