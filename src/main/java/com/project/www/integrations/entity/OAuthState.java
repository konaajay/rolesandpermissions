package com.project.www.integrations.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "integration_oauth_states", indexes = {
        @Index(name = "idx_oauth_state_token", columnList = "state_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state_token", nullable = false, unique = true, length = 64)
    private String stateToken;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "integration_code", nullable = false, length = 50)
    private String integrationCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
