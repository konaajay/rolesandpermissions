package com.project.www.invoice.entity;

import com.project.www.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_configurations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "invoice_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceConfiguration extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "invoice_name", nullable = false, length = 100)
    private String invoiceName;

    @Column(name = "invoice_prefix", length = 20)
    private String invoicePrefix;

    @Column(name = "invoice_number_format", length = 50)
    private String invoiceNumberFormat;

    @Column(name = "company_logo", columnDefinition = "TEXT")
    private String companyLogo;

    @Column(name = "company_details", columnDefinition = "TEXT")
    private String companyDetails;

    @Column(name = "gst_tax_details", columnDefinition = "TEXT")
    private String gstTaxDetails;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Column(name = "target_module", length = 255)
    @Builder.Default
    private String targetModule = "ALL";

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
