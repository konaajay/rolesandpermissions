package com.project.www.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "landing_pages", indexes = {
        @Index(name = "idx_lp_slug", columnList = "slug", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandingPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String headline;
    private String subtitle;
    
    @Column(length = 2000)
    private String description;
    
    // Universal App Routing Fields
    private String moduleType;
    private String landingPageType;
    
    private java.math.BigDecimal price;
    private java.math.BigDecimal adBudget;
    private String videoUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "landing_page_features", joinColumns = @JoinColumn(name = "landing_page_id"))
    @Column(name = "feature")
    private java.util.List<String> features;

    private String ctaText;

    private LocalDateTime createdAt;

    @Transient
    public String getFeaturesJson() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(features);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Transient
    public void setFeaturesJson(String json) {
        try {
            this.features = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            this.features = new java.util.ArrayList<>();
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
