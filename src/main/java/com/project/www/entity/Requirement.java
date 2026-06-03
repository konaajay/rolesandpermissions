package com.project.www.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendor_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "requirement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RequirementItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(nullable = false)
    private String status;

    private LocalDate requiredDate;
    
    private LocalDate returnDate;

    private String requirementType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

}
