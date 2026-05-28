package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "tenant_sequences",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "year"})
    }
)
public class TenantSequence extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Integer year;

    @Builder.Default
    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence = 0L;
}
