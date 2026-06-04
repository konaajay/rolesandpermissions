package com.project.www.integrations.util;

public final class TokenMaskingUtil {

    private TokenMaskingUtil() {
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return "********" + value.substring(value.length() - 4);
    }
}
