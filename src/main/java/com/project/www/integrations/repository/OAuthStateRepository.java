package com.project.www.integrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.OAuthState;

import java.util.Optional;

@Repository
public interface OAuthStateRepository extends JpaRepository<OAuthState, Long> {
    
    Optional<OAuthState> findByStateToken(String stateToken);
}
