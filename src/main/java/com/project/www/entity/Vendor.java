package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vendors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "vendorCode"}),
        @UniqueConstraint(columnNames = {"tenantId", "email"}),
        @UniqueConstraint(columnNames = {"tenantId", "mobileNumber"}),
        @UniqueConstraint(columnNames = {"tenantId", "gstNumber"}),
        @UniqueConstraint(columnNames = {"tenantId", "panNumber"})
})
public class Vendor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String vendorCode;

    @Column(nullable = false, length = 255)
    private String vendorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private VendorCategory category;

    @Column(length = 255)
    private String contactPerson;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String mobileNumber;

    @Column(length = 20)
    private String alternateMobileNumber;

    @Column(length = 255)
    private String companyName;

    @Column(length = 50)
    private String gstNumber;

    @Column(length = 50)
    private String panNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 50)
    private String status; // e.g. ACTIVE, INACTIVE
    
    @Column(name = "rating")
    private Double rating;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true; // For Activate/Deactivate

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false; // For Soft Delete

    @Column(columnDefinition = "TEXT")
    private String documentUrl;
}
