package com.project.www.repository;

import com.project.www.enums.*;

import com.project.www.entity.WalletConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletConfigRepository extends JpaRepository<WalletConfig, Long> {
    Optional<WalletConfig> findTopByOrderByIdAsc();
}
