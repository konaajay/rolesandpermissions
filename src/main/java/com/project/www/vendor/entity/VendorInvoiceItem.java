package com.project.www.vendor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "vendor_invoice_items")
public class VendorInvoiceItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_invoice_id", nullable = false)
    @JsonIgnore
    private VendorInvoice vendorInvoice;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate; // Optional, e.g., 18.00 for 18%

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
