package com.project.www.tenant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "office_locations", indexes = {
        @Index(name = "idx_office_location_name", columnList = "name"),
        @Index(name = "idx_office_location_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private BigDecimal longitude;

    @Column(name = "radius_meters", nullable = false)
    @Positive
    @Builder.Default
    private Double radiusMeters = 30.0;

    @Column(name = "tracking_interval_sec", nullable = false)
    @Positive
    @Builder.Default
    private Integer trackingIntervalSec = 300;

    @Column(name = "max_accuracy_meters", nullable = false)
    @Positive
    @Builder.Default
    private Integer maxAccuracyMeters = 100;

    @Column(name = "max_idle_minutes", nullable = false)
    @Positive
    @Builder.Default
    private Integer maxIdleMinutes = 30;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (radiusMeters == null) {
            radiusMeters = 30.0;
        }

        if (trackingIntervalSec == null) {
            trackingIntervalSec = 300;
        }

        if (maxAccuracyMeters == null) {
            maxAccuracyMeters = 100;
        }

        if (maxIdleMinutes == null) {
            maxIdleMinutes = 30;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}