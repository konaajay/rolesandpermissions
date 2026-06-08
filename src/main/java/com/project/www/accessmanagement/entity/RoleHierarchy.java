package com.project.www.accessmanagement.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "role_hierarchy",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "role_id", "reports_to_role_id"})
    }
)
public class RoleHierarchy extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_to_role_id", nullable = false)
    private Role reportsToRole;
}
