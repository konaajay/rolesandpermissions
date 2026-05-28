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
    name = "user_extra_field_values",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "field_id"})
    }
)
public class UserExtraFieldValue extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private RoleExtraField field;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;
}
