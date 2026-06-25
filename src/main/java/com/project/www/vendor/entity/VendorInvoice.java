package com.project.www.vendor.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vendor_invoices")
public class VendorInvoice extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id")
    private Requirement requirement;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal amountPaid;

    @Column(precision = 15, scale = 2)
    private BigDecimal amountPending;

    private String poRef;

    private String invoiceDate;

    private String dueDate;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(columnDefinition = "TEXT")
    private String receiptUrl;

    @Column(columnDefinition = "TEXT")
    private String paymentHistory;

    @Column(columnDefinition = "TEXT")
    private String customerAddress;

    @Column(length = 50)
    private String gstin;

    @Column(precision = 15, scale = 2)
    private BigDecimal cgst;

    @Column(precision = 15, scale = 2)
    private BigDecimal sgst;

    @Column(precision = 15, scale = 2)
    private BigDecimal igst;

    @Column(precision = 15, scale = 2)
    private BigDecimal discount;

    @Column(precision = 15, scale = 2)
    private BigDecimal subTotal;

    @Column(precision = 15, scale = 2)
    private BigDecimal taxTotal;

    @OneToMany(mappedBy = "vendorInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VendorInvoiceItem> items = new ArrayList<>();
}
