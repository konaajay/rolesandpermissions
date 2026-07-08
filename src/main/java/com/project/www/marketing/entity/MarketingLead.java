package com.project.www.marketing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String courseInterest;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String source;
    private String sessionId;
    private String coupon;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
