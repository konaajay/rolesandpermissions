package com.project.www.integrations.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.project.www.integrations.config.IntegrationProperties;
import com.project.www.integrations.util.AESUtil;
import com.project.www.integrations.util.TokenMaskingUtil;

@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final IntegrationProperties integrationProperties;
    private String secret;

    @PostConstruct
    public void init() {
        secret = integrationProperties.getEncryption().getSecret();
        if (secret == null || secret.isBlank() || "change-this-secret-key".equals(secret)) {
            // Allow dev default but warn - production must override
            secret = secret != null && !secret.isBlank() ? secret : "change-this-secret-key";
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        return AESUtil.encrypt(plainText, secret);
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            return encryptedText;
        }
        return AESUtil.decrypt(encryptedText, secret);
    }

    public String mask(String value) {
        return TokenMaskingUtil.mask(value);
    }
}
