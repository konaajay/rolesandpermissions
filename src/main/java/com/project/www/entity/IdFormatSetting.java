package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "id_format_settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "entity_type"})
})
public class IdFormatSetting extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Builder.Default
    @Column(name = "padding_length", nullable = false)
    private Integer paddingLength = 7;

    @Builder.Default
    @Column(name = "next_sequence", nullable = false)
    private Long nextSequence = 1L;

    @Builder.Default
    @Column(name = "include_year", nullable = false)
    private Boolean includeYear = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
