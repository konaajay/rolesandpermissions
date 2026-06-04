package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracked_links")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackedLinkId;
    private String landingSlug;
    private String source;
    private String medium;
    private String campaign;
    private String generatedLink;
    private BigDecimal adBudget;
    private LocalDateTime timestamp;

}
