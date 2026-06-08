package com.project.www.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "marketing")
public class MarketingProperties {

    private Resend resend = new Resend();
    private Firebase firebase = new Firebase();

    public Resend getResend() {
        return resend;
    }

    public void setResend(Resend resend) {
        this.resend = resend;
    }

    public Firebase getFirebase() {
        return firebase;
    }

    public void setFirebase(Firebase firebase) {
        this.firebase = firebase;
    }

    public static class Resend {
        private String apiKey;
        private String fromEmail;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getFromEmail() {
            return fromEmail;
        }

        public void setFromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
        }
    }

    public static class Firebase {
        private String serverKey;

        public String getServerKey() {
            return serverKey;
        }

        public void setServerKey(String serverKey) {
            this.serverKey = serverKey;
        }
    }
}
