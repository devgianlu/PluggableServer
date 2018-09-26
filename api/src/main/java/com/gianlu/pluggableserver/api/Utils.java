package com.gianlu.pluggableserver.api;

/**
 * @author Gianlu
 */
public final class Utils {

    private Utils() {
    }

    public static int getEnvPort(int fallback) {
        String env = System.getenv("PORT");
        if (env == null || env.isEmpty()) return fallback;

        try {
            return Integer.parseInt(env);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
