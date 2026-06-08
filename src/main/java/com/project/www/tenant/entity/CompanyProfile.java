package com.project.www.tenant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_profiles")
@Data
@NoArgsConstructor
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_code", nullable = false, length = 100, updatable = false)
    private String companyCode;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "pincode", length = 50)
    private String pincode;

    @Column(name = "gst_number", length = 100)
    private String gstNumber;

    @Column(name = "pan_number", length = 100)
    private String panNumber;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "stamp_url", length = 500)
    private String stampUrl;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Column(name = "header_image_url", length = 500)
    private String headerImageUrl;

    @Column(name = "footer_image_url", length = 500)
    private String footerImageUrl;

    @Column(name = "timezone", length = 100)
    private String timezone;

    @Column(name = "currency", length = 50)
    private String currency;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
