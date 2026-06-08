package com.project.www.tenant.entity;

import com.project.www.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tenant_settings")
public class TenantSettings extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(name = "employee_id_format")
    private String employeeIdFormat;

    @Column(name = "lead_id_format")
    private String leadIdFormat;

    @Builder.Default
    @Column(name = "employee_sequence")
    private Long employeeSequence = 0L;

    @Builder.Default
    @Column(name = "lead_sequence")
    private Long leadSequence = 0L;
}
