package com.project.www.config;

import org.springframework.stereotype.Component;

@Component
public class MarketingProperties {
    public Resend getResend() {
        return new Resend();
    }
    
    public Firebase getFirebase() {
        return new Firebase();
    }
    
    public static class Resend {
        public String getApiKey() {
            return "";
        }
        
        public String getFromEmail() {
            return "";
        }
    }
    
    public static class Firebase {
        public boolean isEnabled() { return false; }
        public String getServerKey() { return ""; }
    }
}
