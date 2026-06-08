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
    name = "role_extra_fields",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "field_name"})
    }
)
public class RoleExtraField extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "field_label", nullable = false)
    private String fieldLabel;

    @Column(name = "field_type", nullable = false)
    private String fieldType;

    @Builder.Default
    @Column(nullable = false)
    private Boolean required = false;

    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;

    @Builder.Default
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
