package com.project.www.vendor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "asset_type")
    private String assetType;
    
    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "purchase_date")
    private String purchaseDate;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
}
