package com.project.www.marketing.entity;

import com.project.www.entity.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interactions", indexes = {
        @Index(name = "idx_interaction_campaign", columnList = "campaign_id"),
        @Index(name = "idx_interaction_user", columnList = "customer_email"),
        @Index(name = "idx_interaction_content", columnList = "content_id"),
        @Index(name = "idx_interaction_date", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "content_id")
    private Long contentId;

    @Column(length = 20)
    private String actionType; // CLICK, VIEW, DOWNLOAD

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
