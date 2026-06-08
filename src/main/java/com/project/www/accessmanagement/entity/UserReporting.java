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
    name = "user_reporting",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "user_id", "supervisor_user_id"})
    }
)
public class UserReporting extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_user_id", nullable = false)
    private User supervisorUser;
}
