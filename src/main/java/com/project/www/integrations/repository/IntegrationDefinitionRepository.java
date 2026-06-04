package com.project.www.integrations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.www.integrations.entity.IntegrationDefinition;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationDefinitionRepository extends JpaRepository<IntegrationDefinition, Long> {

    Optional<IntegrationDefinition> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    Optional<IntegrationDefinition> findByCode(String code);

    List<IntegrationDefinition> findByActiveTrue();
}
