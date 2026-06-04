package com.project.www.dto;

import com.project.www.enums.*;

import com.project.www.enums.EmailStatus;

public class EmailProviderResult {
    private EmailStatus status;
    private String messageId;
    private String error;
    private boolean success;

    public EmailProviderResult() {}

    public EmailProviderResult(EmailStatus status, String messageId, String error, boolean success) {
        this.status = status;
        this.messageId = messageId;
        this.error = error;
        this.success = success;
    }

    // Manual Getters 🛡️🏁
    public EmailStatus getStatus() { return status; }
    public String getMessageId() { return messageId; }
    public String getError() { return error; }
    public boolean isSuccess() { return success; }

    // Manual Builder to resolve Lombok failure 🛡️🚀
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private EmailStatus status;
        private String messageId;
        private String error;
        private boolean success;

        public Builder status(EmailStatus status) { this.status = status; return this; }
        public Builder messageId(String id) { this.messageId = id; return this; }
        public Builder error(String error) { this.error = error; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public EmailProviderResult build() { return new EmailProviderResult(status, messageId, error, success); }
    }
}
