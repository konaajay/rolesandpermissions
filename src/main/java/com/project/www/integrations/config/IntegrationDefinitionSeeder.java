package com.project.www.integrations.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.project.www.integrations.entity.IntegrationDefinition;
import com.project.www.integrations.repository.IntegrationDefinitionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IntegrationDefinitionSeeder implements CommandLineRunner {

    private final IntegrationDefinitionRepository repository;

    @Override
    public void run(String... args) {
        // Seed each integration with full metadata
        seed("API_KEY", "API Key", "AUTHENTICATION", "INTERNAL", "#6366F1", "key",
                "Generate API keys for external systems to access selected public APIs securely.");
        seed("GOOGLE", "Google", "PRODUCTIVITY", "GOOGLE", "#4285F4", "google",
                "Connect Google services like Calendar, Gmail, and Sheets.");
        seed("ZOOM", "Zoom", "COMMUNICATION", "ZOOM", "#2D8CFF", "video",
                "Connect Zoom meetings and video conferencing.");
        seed("ZAPIER", "Zapier", "AUTOMATION", "ZAPIER", "#FF4A00", "zap",
                "Send events to Zapier automations.");
        seed("CASHFREE", "Cashfree", "PAYMENT", "CASHFREE", "#00A86B", "credit-card",
                "Accept and verify payments using Cashfree.");
        seed("WEBHOOK", "Webhook", "AUTOMATION", "INTERNAL", "#8B5CF6", "webhook",
                "Send platform events to external systems.");
        seed("META", "Meta", "SOCIAL", "META", "#1877F2", "facebook",
                "Connect Facebook and Instagram services.");
        seed("WHATSAPP", "WhatsApp", "COMMUNICATION", "META", "#25D366", "message-circle",
                "Connect WhatsApp Business messaging.");
    }

    private void seed(String code, String name, String category, String provider,
                      String color, String icon, String description) {
        if (!repository.existsByCodeIgnoreCase(code)) {
            IntegrationDefinition def = new IntegrationDefinition();
            def.setCode(code);
            def.setName(name);
            def.setCategory(category);
            def.setProvider(provider);
            def.setColor(color);
            def.setIcon(icon);
            def.setDescription(description);
            def.setActive(true);
            repository.save(def);
        }
    }
}
