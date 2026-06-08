package com.project.www.vendor.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vendor_contracts")
public class VendorContract extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    private String startDate;

    private String expires;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
