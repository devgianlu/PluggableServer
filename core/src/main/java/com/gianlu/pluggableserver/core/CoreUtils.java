package com.gianlu.pluggableserver.core;

import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;

/**
 * @author Gianlu
 */
public final class CoreUtils {

    private CoreUtils() {
    }

    @Nullable
    public static String getParam(@NotNull HttpServerExchange exchange, @NotNull String name) {
        Deque<String> param = exchange.getQueryParameters().get(name);
        if (param == null) return null;
        else return param.getFirst();
    }

    public static String getEnv(@NotNull String name, String fallback) {
        String env = System.getenv(name);
        if (env == null || env.isEmpty()) return fallback;
        return env;
    }
}
