package com.project.www.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requirement_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequirementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id")
    @JsonIgnore
    private Requirement requirement;

    @Column(nullable = false)
    private String itemName;

    private String brand;

    @Column(nullable = false)
    private Integer quantity;

    private String unit;
}
