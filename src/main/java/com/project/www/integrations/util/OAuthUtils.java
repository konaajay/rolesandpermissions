package com.project.www.integrations.util;

/**
 * Utility methods for OAuth related helper functions.
 */
public class OAuthUtils {

    /**
     * Extract the tenant ID from the OAuth {@code state} parameter.
     * The expected format is {@code "{tenantId}:{timestamp}"}.
     *
     * @param state the state string received from the OAuth provider
     * @return the tenant ID as a {@link Long}
     * @throws IllegalArgumentException if the state is null, does not contain a colon, or the tenant ID is not a valid number
     */
    public static Long extractTenantId(String state) {
        if (state == null || !state.contains(":")) {
            throw new IllegalArgumentException("Invalid OAuth state");
        }
        try {
            return Long.parseLong(state.split(":")[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid tenant ID in OAuth state", e);
        }
    }
}
