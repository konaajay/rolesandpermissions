package com.project.www.integrations.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public final class HmacUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private HmacUtil() {
    }

    public static String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC signing failed", e);
        }
    }

    public static boolean verify(String payload, String secret, String signature) {
        if (signature == null) {
            return false;
        }
        return sign(payload, secret).equalsIgnoreCase(signature);
    }
}
