package com.project.www.integrations.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class ApiKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private ApiKeyGenerator() {
    }

    public static String generateApiKey() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "usk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateApiSecret() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return "uss_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }
}
