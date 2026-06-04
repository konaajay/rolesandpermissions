package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leadId;

    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    private String changedBy;

    private String notes;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
