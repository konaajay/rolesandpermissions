package com.project.www.integrations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.www.integrations.entity.OAuthState;
import com.project.www.integrations.repository.OAuthStateRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private final OAuthStateRepository oauthStateRepository;

    @Transactional("integrationTransactionManager")
    public String generateState(Long tenantId, String integrationCode) {
        String stateToken = UUID.randomUUID().toString();
        
        OAuthState oauthState = OAuthState.builder()
                .stateToken(stateToken)
                .tenantId(tenantId)
                .integrationCode(integrationCode)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
                
        oauthStateRepository.save(oauthState);
        return stateToken;
    }

    @Transactional("integrationTransactionManager")
    public Long validateAndExtractTenantId(String stateToken, String expectedIntegrationCode) {
        if (stateToken == null || stateToken.isBlank()) {
            throw new IllegalArgumentException("OAuth state is missing or empty.");
        }

        OAuthState state = oauthStateRepository.findByStateToken(stateToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OAuth state: Token not found or invalid."));

        if (!state.getIntegrationCode().equalsIgnoreCase(expectedIntegrationCode)) {
            throw new IllegalArgumentException("Invalid OAuth state: Integration code mismatch.");
        }

        if (state.isUsed()) {
            throw new IllegalArgumentException("Invalid OAuth state: This token has already been used (Replay attack).");
        }

        if (state.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid OAuth state: Token has expired.");
        }

        // Mark as used to prevent replay
        state.setUsed(true);
        oauthStateRepository.save(state);

        return state.getTenantId();
    }
}
