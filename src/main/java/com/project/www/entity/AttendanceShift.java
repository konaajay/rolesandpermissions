package com.project.www.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_shifts", indexes = {
        @Index(name = "idx_shift_name", columnList = "name"),
        @Index(name = "idx_shift_tenant", columnList = "tenant_id"),
        @Index(name = "idx_shift_office", columnList = "office_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_minutes", nullable = false)
    @Positive
    @Builder.Default
    private Integer graceMinutes = 15;

    @Column(name = "min_half_day_minutes", nullable = false)
    @Positive
    @Builder.Default
    private Integer minHalfDayMinutes = 240;

    @Column(name = "min_full_day_minutes", nullable = false)
    @Positive
    @Builder.Default
    private Integer minFullDayMinutes = 480;

    @Column(name = "short_break_start_time")
    private LocalTime shortBreakStartTime;

    @Column(name = "short_break_end_time")
    private LocalTime shortBreakEndTime;

    @Column(name = "long_break_start_time")
    private LocalTime longBreakStartTime;

    @Column(name = "long_break_end_time")
    private LocalTime longBreakEndTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OfficeLocation office;

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

        if (graceMinutes == null) {
            graceMinutes = 15;
        }

        if (minHalfDayMinutes == null) {
            minHalfDayMinutes = 240;
        }

        if (minFullDayMinutes == null) {
            minFullDayMinutes = 480;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}