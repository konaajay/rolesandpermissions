package com.project.www.integrations.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.www.integrations.entity.IntegrationDefinition;
import com.project.www.integrations.entity.TenantIntegration;
import com.project.www.integrations.enums.IntegrationHealth;
import com.project.www.integrations.enums.IntegrationStatus;
import com.project.www.integrations.repository.IntegrationDefinitionRepository;
import com.project.www.integrations.repository.TenantIntegrationRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Ensures that the {@code API_KEY} integration definition and a default tenant integration
 * (tenantId = 1) exist when the application starts.
 *
 * The seeder is idempotent – it will not create duplicate rows if they are already present.
 */
@Component
public class IntegrationDataSeeder implements CommandLineRunner {

    private final IntegrationDefinitionRepository definitionRepository;
    private final TenantIntegrationRepository tenantRepository;

    public IntegrationDataSeeder(IntegrationDefinitionRepository definitionRepository,
                                TenantIntegrationRepository tenantRepository) {
        this.definitionRepository = definitionRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Ensure IntegrationDefinition for API_KEY exists
        Optional<IntegrationDefinition> optDef = definitionRepository.findByCode("API_KEY");
        IntegrationDefinition definition = optDef.orElseGet(() -> {
            IntegrationDefinition def = IntegrationDefinition.builder()
                    .code("API_KEY")
                    .name("API Key")
                    .category("AUTHENTICATION")
                    .provider("INTERNAL")
                    .description("Generate API keys for external systems to access selected public APIs securely.")
                    .icon("key")
                    .color("#6366F1")
                    .active(true)
                    .build();
            return definitionRepository.save(def);
        });

        // 2. Ensure TenantIntegration for tenantId = 1 and code = API_KEY exists
        Long tenantId = 1L;
        boolean exists = tenantRepository.existsByTenantIdAndCode(tenantId, "API_KEY");
        if (!exists) {
            TenantIntegration ti = TenantIntegration.builder()
                    .tenantId(tenantId)
                    .integrationDefinitionId(definition.getId())
                    .code("API_KEY")
                    .enabled(true)
                    .connected(true)
                    .status(IntegrationStatus.CONNECTED)
                    .health(IntegrationHealth.HEALTHY)
                    .environment("development")
                    .connectedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            tenantRepository.save(ti);
        }
    }
}
