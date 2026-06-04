package com.project.www.integrations.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private Encryption encryption = new Encryption();
    private Webhook webhook = new Webhook();
    private Logs logs = new Logs();

    @Data
    public static class Encryption {
        private String secret;
    }

    @Data
    public static class Webhook {
        private int maxRetry = 3;
        private int retryDelayMinutes = 5;
    }

    @Data
    public static class Logs {
        private int retentionDays = 90;
    }
    private Google google = new Google();
    private Zoom zoom = new Zoom();
    private Cashfree cashfree = new Cashfree();
    private Meta meta = new Meta();
    private Whatsapp whatsapp = new Whatsapp();
    private Zapier zapier = new Zapier();

    @Data
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    @Data
    public static class Zoom {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    @Data
    public static class Cashfree {
        private String clientId;
        private String clientSecret;
        private String sandboxBaseUrl;
        private String productionBaseUrl;
        private String webhookUrl;
    }

    @Data
    public static class Meta {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String pageAccessToken;
    }

    @Data
    public static class Whatsapp {
        private String clientId;
        private String clientSecret;
        private String phoneNumberId;
        private String webhookUrl;
    }

    @Data
    public static class Zapier {
        private String apiKey;
        private String webhookUrl;
    }

}
