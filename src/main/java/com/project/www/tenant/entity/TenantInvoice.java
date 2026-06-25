package com.project.www.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "invoice_type")
    private String invoiceType; // NEW_SUBSCRIPTION, RENEWAL, ADDON_MODULE

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "gst_amount")
    private Double gstAmount;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "pending_amount")
    private Double pendingAmount;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status")
    private String status;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "gstin", length = 50)
    private String gstin;

    @Column(name = "cgst")
    private Double cgst;

    @Column(name = "sgst")
    private Double sgst;

    @Column(name = "igst")
    private Double igst;

    @Column(name = "discount")
    private Double discount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
